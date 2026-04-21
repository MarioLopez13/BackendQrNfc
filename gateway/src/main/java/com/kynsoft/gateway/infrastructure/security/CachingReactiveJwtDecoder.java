package com.kynsoft.gateway.infrastructure.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.kynsoft.gateway.infrastructure.config.JwtCacheProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Implementacion de ReactiveJwtDecoder que cachea tokens JWT validados
 * para evitar la validacion criptografica en cada peticion.
 *
 * Caracteristicas:
 * - Cache en memoria usando Caffeine (alta performance)
 * - Usa hash SHA-256 del token como clave (seguridad)
 * - TTL dinamico basado en expiracion del token
 * - Metricas de cache disponibles via actuator
 */
@Slf4j
public class CachingReactiveJwtDecoder implements ReactiveJwtDecoder {

    private final ReactiveJwtDecoder delegate;
    private final Cache<String, Jwt> tokenCache;
    private final Cache<String, CachedUserInfo> userInfoCache;
    private final boolean cacheEnabled;
    private final int maxTtlMinutes;

    public CachingReactiveJwtDecoder(String issuerUri, JwtCacheProperties properties) {
        this.delegate = ReactiveJwtDecoders.fromIssuerLocation(issuerUri);
        this.cacheEnabled = properties.isEnabled();
        this.maxTtlMinutes = properties.getExpireAfterWriteMinutes();

        if (cacheEnabled) {
            this.tokenCache = Caffeine.newBuilder()
                    .maximumSize(properties.getMaxSize())
                    .expireAfterWrite(properties.getExpireAfterWriteMinutes(), TimeUnit.MINUTES)
                    .recordStats()
                    .build();
            this.userInfoCache = Caffeine.newBuilder()
                    .maximumSize(properties.getMaxSize())
                    .expireAfterWrite(properties.getExpireAfterWriteMinutes(), TimeUnit.MINUTES)
                    .build();
            log.info("JWT Token Cache inicializado - maxSize: {}, ttl: {} minutos",
                    properties.getMaxSize(), properties.getExpireAfterWriteMinutes());
        } else {
            this.tokenCache = null;
            this.userInfoCache = null;
            log.info("JWT Token Cache deshabilitado");
        }
    }

    @Override
    public Mono<Jwt> decode(String token) throws JwtException {
        if (!cacheEnabled || tokenCache == null) {
            return delegate.decode(token);
        }

        String cacheKey = hashToken(token);

        // Intentar obtener del cache
        Jwt cachedJwt = tokenCache.getIfPresent(cacheKey);
        if (cachedJwt != null) {
            // Verificar que el token cacheado no haya expirado
            if (!isExpired(cachedJwt)) {
                log.debug("JWT cache HIT para token hash: {}...", cacheKey.substring(0, 8));
                return Mono.just(cachedJwt);
            } else {
                // Token expirado, remover del cache
                tokenCache.invalidate(cacheKey);
                log.debug("JWT cache EXPIRED para token hash: {}...", cacheKey.substring(0, 8));
            }
        }

        // Cache miss - decodificar y cachear
        return delegate.decode(token)
                .doOnNext(jwt -> {
                    long ttlSeconds = calculateTtlSeconds(jwt);
                    if (ttlSeconds > 30) { // Solo cachear si tiene mas de 30 segundos de vida
                        tokenCache.put(cacheKey, jwt);
                        log.debug("JWT cache PUT para token hash: {}..., TTL: {}s",
                                cacheKey.substring(0, 8), ttlSeconds);
                    }
                })
                .doOnError(error -> log.debug("JWT decode error: {}", error.getMessage()));
    }

    /**
     * Genera un hash SHA-256 del token para usarlo como clave de cache.
     * Esto evita almacenar el token completo en el cache.
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 siempre esta disponible en Java
            throw new RuntimeException("SHA-256 no disponible", e);
        }
    }

    /**
     * Verifica si el token JWT ha expirado.
     */
    private boolean isExpired(Jwt jwt) {
        Instant expiresAt = jwt.getExpiresAt();
        if (expiresAt == null) {
            return false; // Si no tiene exp, asumimos que no expira
        }
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * Calcula el TTL en segundos para el cache.
     * Usa el minimo entre el tiempo restante del token y el TTL maximo configurado.
     */
    private long calculateTtlSeconds(Jwt jwt) {
        Instant expiresAt = jwt.getExpiresAt();
        if (expiresAt == null) {
            return maxTtlMinutes * 60L;
        }

        long tokenTtlSeconds = Duration.between(Instant.now(), expiresAt).getSeconds();
        long maxTtlSeconds = maxTtlMinutes * 60L;

        return Math.min(tokenTtlSeconds, maxTtlSeconds);
    }

    /**
     * Obtiene estadisticas del cache para monitoreo.
     */
    public CacheStats getCacheStats() {
        if (tokenCache == null) {
            return CacheStats.empty();
        }
        return tokenCache.stats();
    }

    /**
     * Obtiene el tamano actual del cache.
     */
    public long getCacheSize() {
        if (tokenCache == null) {
            return 0;
        }
        return tokenCache.estimatedSize();
    }

    /**
     * Invalida todo el cache. Util para testing o reinicio manual.
     */
    public void invalidateAll() {
        if (tokenCache != null) {
            tokenCache.invalidateAll();
        }
        if (userInfoCache != null) {
            userInfoCache.invalidateAll();
        }
        log.info("JWT Token Cache y UserInfo Cache invalidados completamente");
    }

    /**
     * Indica si el cache esta habilitado.
     */
    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    /**
     * Genera el hash del token para usarlo como clave de cache.
     * Metodo publico para que el filtro pueda usar la misma clave.
     */
    public String getTokenHash(String token) {
        return hashToken(token);
    }

    /**
     * Obtiene la informacion del usuario desde el cache.
     * @param tokenHash Hash del token (obtenido con getTokenHash)
     * @return CachedUserInfo si existe en cache, null si no
     */
    public CachedUserInfo getCachedUserInfo(String tokenHash) {
        if (userInfoCache == null) {
            return null;
        }
        return userInfoCache.getIfPresent(tokenHash);
    }

    /**
     * Cachea la informacion del usuario extraida del JWT.
     * @param tokenHash Hash del token
     * @param jwt El JWT decodificado
     * @param authorities Los roles/authorities del usuario
     */
    public void cacheUserInfo(String tokenHash, Jwt jwt, Collection<? extends GrantedAuthority> authorities) {
        if (userInfoCache == null || jwt == null) {
            return;
        }

        String userId = jwt.getClaimAsString("sub");
        String userName = jwt.getClaimAsString("preferred_username");
        String email = jwt.getClaimAsString("email");
        String roles = authorities != null ?
            authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")) : "";

        CachedUserInfo userInfo = new CachedUserInfo(userId, userName, email, roles);
        userInfoCache.put(tokenHash, userInfo);
        log.debug("UserInfo cacheado para token hash: {}...", tokenHash.substring(0, 8));
    }

    /**
     * Obtiene el tamano del cache de UserInfo.
     */
    public long getUserInfoCacheSize() {
        if (userInfoCache == null) {
            return 0;
        }
        return userInfoCache.estimatedSize();
    }
}