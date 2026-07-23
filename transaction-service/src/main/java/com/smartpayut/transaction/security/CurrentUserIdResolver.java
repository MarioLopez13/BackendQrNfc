package com.smartpayut.transaction.security;

import java.util.UUID;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserIdResolver {

    public UUID resolve(Jwt jwt) {
        Object claim = jwt.getClaim("user_id");
        if (claim == null) {
            claim = jwt.getClaim("userId");
        }
        String value = claim == null ? jwt.getSubject() : claim.toString();
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("El JWT no contiene un identificador de usuario válido.");
        }
    }
}
