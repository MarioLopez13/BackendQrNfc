package com.smartpayut.identity.service;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartpayut.identity.domain.entity.UserAccount;
import com.smartpayut.identity.domain.entity.UserProfile;
import com.smartpayut.identity.dto.response.UserResponse;
import com.smartpayut.identity.event.IdentityUserCreatedEvent;
import com.smartpayut.identity.exception.IdentityException;
import com.smartpayut.identity.mapper.UserAccountMapper;
import com.smartpayut.identity.mapper.UserProfileMapper;
import com.smartpayut.identity.messaging.publisher.IdentityEventPublisher;
import com.smartpayut.identity.repository.UserAccountRepository;
import com.smartpayut.identity.repository.UserProfileRepository;

@Service
public class UserReconciliationService {

    private final UserAccountRepository accounts;
    private final UserProfileRepository profiles;
    private final IdentityEventPublisher events;
    private final UserAccountMapper accountMapper;
    private final UserProfileMapper profileMapper;

    public UserReconciliationService(
            UserAccountRepository accounts,
            UserProfileRepository profiles,
            IdentityEventPublisher events,
            UserAccountMapper accountMapper,
            UserProfileMapper profileMapper) {
        this.accounts = accounts;
        this.profiles = profiles;
        this.events = events;
        this.accountMapper = accountMapper;
        this.profileMapper = profileMapper;
    }

    @Transactional
    public UserResponse reconcile(Jwt jwt) {
        UUID keycloakId = keycloakId(jwt.getSubject());
        return accounts.findByKeycloakId(keycloakId)
                .map(this::response)
                .orElseGet(() -> create(jwt, keycloakId));
    }

    private UserResponse create(Jwt jwt, UUID keycloakId) {
        String email = requiredClaim(jwt, "email").toLowerCase(Locale.ROOT);
        String userName = claimOrDefault(jwt, "preferred_username", email).toLowerCase(Locale.ROOT);
        String name = claimOrDefault(jwt, "given_name", userName);
        String lastName = claimOrDefault(jwt, "family_name", "");

        if (accounts.findByEmailIgnoreCase(email).isPresent()) {
            throw new IdentityException(
                    HttpStatus.CONFLICT,
                    "El correo ya pertenece a otra cuenta local");
        }
        if (accounts.existsByUserNameIgnoreCase(userName)) {
            throw new IdentityException(
                    HttpStatus.CONFLICT,
                    "El username ya pertenece a otra cuenta local");
        }

        try {
            UserAccount account = accounts.saveAndFlush(new UserAccount(
                    UUID.randomUUID(),
                    keycloakId,
                    userName,
                    email,
                    name,
                    lastName));
            UserProfile profile = profiles.saveAndFlush(profileMapper.create(account));
            events.publish(new IdentityUserCreatedEvent(
                    UUID.randomUUID(),
                    "identity.user.created",
                    1,
                    OffsetDateTime.now(),
                    account.getId(),
                    keycloakId,
                    account.getUserName(),
                    account.getEmail(),
                    account.getName(),
                    account.getLastName(),
                    account.getStatus().name()));
            return accountMapper.toResponse(account, profile);
        } catch (DataIntegrityViolationException exception) {
            throw new IdentityException(
                    HttpStatus.CONFLICT,
                    "La cuenta local ya fue reconciliada");
        }
    }

    private UserResponse response(UserAccount account) {
        return accountMapper.toResponse(
                account,
                profiles.findByUserAccountId(account.getId()).orElse(null));
    }

    private UUID keycloakId(String subject) {
        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new IdentityException(
                    HttpStatus.UNAUTHORIZED,
                    "JWT sin subject válido");
        }
    }

    private String requiredClaim(Jwt jwt, String claimName) {
        String value = jwt.getClaimAsString(claimName);
        if (value == null || value.isBlank()) {
            throw new IdentityException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "JWT sin claim " + claimName);
        }
        return value.trim();
    }

    private String claimOrDefault(Jwt jwt, String claimName, String defaultValue) {
        String value = jwt.getClaimAsString(claimName);
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }
}
