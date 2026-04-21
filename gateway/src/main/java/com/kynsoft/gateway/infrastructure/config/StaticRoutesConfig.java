package com.kynsoft.gateway.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StaticRoutesConfig {

    @Value("${services.identity.url:http://localhost:9909}")
    private String identityServiceUrl;

    /**
     * Aplica filtros comunes a todas las rutas del gateway:
     * - stripPrefix(1): Elimina el primer segmento del path
     * - dedupeResponseHeader: Elimina headers duplicados de CORS
     */
    private GatewayFilterSpec applyCommonFilters(GatewayFilterSpec f) {
        return f.stripPrefix(1)
                .dedupeResponseHeader("Access-Control-Allow-Credentials", "RETAIN_FIRST")
                .dedupeResponseHeader("Access-Control-Allow-Origin", "RETAIN_FIRST")
                .dedupeResponseHeader("Access-Control-Request-Headers", "RETAIN_FIRST");
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Identity Service
                .route("identity", r -> r.path("/identity/**")
                        .filters(this::applyCommonFilters)
                        .uri(identityServiceUrl))
                .build();
    }
}
