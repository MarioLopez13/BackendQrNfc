package com.kynsoft.gateway.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "gateway.jwt-cache")
@Getter
@Setter
public class JwtCacheProperties {

    /**
     * Habilita o deshabilita el cache de tokens JWT.
     */
    private boolean enabled = true;

    /**
     * Numero maximo de tokens a almacenar en cache.
     */
    private int maxSize = 10000;

    /**
     * Tiempo maximo en minutos que un token permanece en cache.
     * El TTL real sera el minimo entre este valor y el tiempo de expiracion del token.
     */
    private int expireAfterWriteMinutes = 5;
}