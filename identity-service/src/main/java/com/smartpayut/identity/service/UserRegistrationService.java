package com.smartpayut.identity.service;

import com.smartpayut.identity.domain.entity.*;
import com.smartpayut.identity.dto.request.UserRegistrationRequest;
import com.smartpayut.identity.dto.response.UserResponse;
import com.smartpayut.identity.event.IdentityUserCreatedEvent;
import com.smartpayut.identity.exception.IdentityException;
import com.smartpayut.identity.mapper.*;
import com.smartpayut.identity.messaging.publisher.IdentityEventPublisher;
import com.smartpayut.identity.service.keycloak.KeycloakClient;
import com.smartpayut.identity.repository.*;
import org.slf4j.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class UserRegistrationService {
    private static final Logger log = LoggerFactory.getLogger(UserRegistrationService.class);
    private final UserAccountRepository accounts;
    private final UserProfileRepository profiles;
    private final KeycloakClient keycloak;
    private final IdentityEventPublisher events;
    private final UserAccountMapper mapper;
    private final UserProfileMapper profileMapper;

    public UserRegistrationService(UserAccountRepository a, UserProfileRepository p, KeycloakClient k,
            IdentityEventPublisher e, UserAccountMapper m, UserProfileMapper pm) {
        accounts = a;
        profiles = p;
        keycloak = k;
        events = e;
        mapper = m;
        profileMapper = pm;
    }

    @Transactional
    public UserResponse register(UserRegistrationRequest request) {
        String email = request.email().trim().toLowerCase(), username = request.userName().trim().toLowerCase();
        if (accounts.existsByEmailIgnoreCase(email))
            throw new IdentityException(HttpStatus.CONFLICT, "El email ya está registrado");
        if (accounts.existsByUserNameIgnoreCase(username))
            throw new IdentityException(HttpStatus.CONFLICT, "El username ya está registrado");
        UUID userAccountId = UUID.randomUUID();
        UUID kid = keycloak.createUser(new UserRegistrationRequest(username, email, request.name().trim(),
                request.lastName().trim(), request.password()), userAccountId);
        try {
            UserAccount a = accounts.saveAndFlush(new UserAccount(userAccountId, kid, username, email,
                    request.name().trim(), request.lastName().trim()));
            UserProfile p = profiles.saveAndFlush(profileMapper.create(a));
            events.publish(new IdentityUserCreatedEvent(UUID.randomUUID(), "identity.user.created", 1,
                    OffsetDateTime.now(), a.getId(), kid, a.getUserName(), a.getEmail(), a.getName(), a.getLastName(),
                    a.getStatus().name()));
            return mapper.toResponse(a, p);
        } catch (RuntimeException failure) {
            try {
                keycloak.deleteUser(kid);
            } catch (RuntimeException compensation) {
                log.error("Falló compensación de Keycloak para {}", kid, compensation);
                failure.addSuppressed(compensation);
            }
            if (failure instanceof IdentityException i)
                throw i;
            if (failure instanceof DataIntegrityViolationException)
                throw new IdentityException(HttpStatus.CONFLICT, "El usuario ya existe");
            throw failure;
        }
    }
}
