package com.smartpayut.identity.service;

import com.smartpayut.identity.domain.entity.*;
import com.smartpayut.identity.domain.enumeration.UserStatus;
import com.smartpayut.identity.dto.request.UserSearchRequest;
import com.smartpayut.identity.dto.response.*;
import com.smartpayut.identity.exception.IdentityException;
import com.smartpayut.identity.mapper.UserAccountMapper;
import com.smartpayut.identity.repository.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class UserQueryService {
    private final UserAccountRepository accounts;
    private final UserProfileRepository profiles;
    private final UserAccountMapper mapper;

    public UserQueryService(UserAccountRepository a, UserProfileRepository p, UserAccountMapper m) {
        accounts = a;
        profiles = p;
        mapper = m;
    }

    public UserResponse byId(UUID id) {
        return map(accounts.findById(id)
                .orElseThrow(() -> new IdentityException(HttpStatus.NOT_FOUND, "Usuario no encontrado")));
    }

    public UserResponse me(String subject) {
        UUID keycloakId;
        try {
            keycloakId = UUID.fromString(subject);
        } catch (Exception e) {
            throw new IdentityException(HttpStatus.UNAUTHORIZED, "JWT sin subject válido");
        }
        return map(accounts.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new IdentityException(HttpStatus.NOT_FOUND, "Cuenta local no encontrada")));
    }

    public PaginatedResponse<UserResponse> search(UserSearchRequest request) {
        String q = request.query() == null ? "" : request.query().trim().toLowerCase();
        Specification<UserAccount> spec = (root, query, cb) -> q.isBlank() ? cb.conjunction()
                : cb.or(cb.like(cb.lower(root.get("userName")), "%" + q + "%"),
                        cb.like(cb.lower(root.get("email")), "%" + q + "%"),
                        cb.like(cb.lower(root.get("name")), "%" + q + "%"),
                        cb.like(cb.lower(root.get("lastName")), "%" + q + "%"));
        Page<UserAccount> page = accounts.findAll(spec,
                PageRequest.of(request.resolvedPage(), request.resolvedSize(), Sort.by("createdAt").descending()));
        return new PaginatedResponse<>(page.stream().map(this::map).toList(), page.getTotalElements(), page.getNumber(),
                page.getSize(), page.getTotalPages());
    }

    public UserSummaryResponse summary() {
        return new UserSummaryResponse(
                accounts.count(),
                accounts.countByStatus(UserStatus.ACTIVE),
                accounts.countByStatus(UserStatus.INACTIVE),
                accounts.countByStatus(UserStatus.DELETED));
    }

    private UserResponse map(UserAccount a) {
        return mapper.toResponse(a, profiles.findByUserAccountId(a.getId()).orElse(null));
    }
}
