package com.kynsoft.gateway.infrastructure.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Informacion del usuario extraida del JWT y cacheada.
 */
@Getter
@AllArgsConstructor
public class CachedUserInfo {
    private final String userId;
    private final String userName;
    private final String email;
    private final String roles;
}
