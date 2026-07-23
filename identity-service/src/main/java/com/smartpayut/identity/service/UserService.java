package com.smartpayut.identity.service;

import com.smartpayut.identity.domain.entity.*;
import com.smartpayut.identity.dto.request.*;
import com.smartpayut.identity.dto.response.UserResponse;
import com.smartpayut.identity.exception.IdentityException;
import com.smartpayut.identity.mapper.UserAccountMapper;
import com.smartpayut.identity.service.keycloak.KeycloakClient;
import com.smartpayut.identity.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class UserService {
    private final UserAccountRepository accounts;
    private final UserProfileRepository profiles;
    private final KeycloakClient keycloak;
    private final UserAccountMapper mapper;

    public UserService(UserAccountRepository a, UserProfileRepository p, KeycloakClient k, UserAccountMapper m) {
        accounts = a;
        profiles = p;
        keycloak = k;
        mapper = m;
    }

    @Transactional
    public UserResponse update(UUID id, UserUpdateRequest r) {
        UserAccount a = accounts.findById(id)
                .orElseThrow(() -> new IdentityException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        if (r.email() != null && !r.email().equalsIgnoreCase(a.getEmail())
                && accounts.existsByEmailIgnoreCase(r.email()))
            throw new IdentityException(HttpStatus.CONFLICT, "El email ya está registrado");
        keycloak.updateUser(a.getKeycloakId(), r.email(), r.name(), r.lastName(),
                r.status() == null ? null : r.status().name().equals("ACTIVE"));
        a.update(r.name(), r.lastName(), r.email(), r.status());
        UserProfile p = profiles.findByUserAccountId(id).orElseGet(() -> new UserProfile(UUID.randomUUID(), a));
        p.update(r.phone(), r.image(), r.preferredLanguage());
        profiles.save(p);
        return mapper.toResponse(a, p);
    }

    @Transactional
    public void delete(DeleteAccountRequest r, String subject) {
        UserAccount a = accounts.findByEmailIgnoreCase(r.username())
                .orElseThrow(() -> new IdentityException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        if (!a.getKeycloakId().toString().equals(subject))
            throw new IdentityException(HttpStatus.FORBIDDEN, "No puede eliminar otra cuenta");
        keycloak.authenticate(r.username(), r.password());
        keycloak.deleteUser(a.getKeycloakId());
        a.markDeleted();
    }

    @Transactional
    public void deleteById(UUID id) {
        UserAccount a = accounts.findById(id)
                .orElseThrow(() -> new IdentityException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        keycloak.deleteUser(a.getKeycloakId());
        a.markDeleted();
    }
}
