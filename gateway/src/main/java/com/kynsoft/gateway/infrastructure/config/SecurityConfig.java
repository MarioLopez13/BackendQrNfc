package com.kynsoft.gateway.infrastructure.config;

import com.kynsoft.gateway.infrastructure.security.CachingReactiveJwtDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static final String REQUIRED_HEADER = "X-Client-Token";

    @Value("${gateway.security.expected-header-value:pQfoROQs2QG0WuXwLvuCHocprzq87w774sF5XtVhuMU}")
    private String expectedHeaderValue;

    @Value("${gateway.security.enable-header-validation:true}")
    private boolean enableHeaderValidation;

    @Value("${gateway.security.url-validation:http://localhost:8180/realms/smartpayut}")
    private String urlMethodValidation;

    private final JwtCacheProperties jwtCacheProperties;

    public SecurityConfig(JwtCacheProperties jwtCacheProperties) {
        this.jwtCacheProperties = jwtCacheProperties;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity httpSecurity) {
        return httpSecurity
                .cors(cors -> cors.disable())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/identity/api/auth/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/identity/api/auth/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/identity/api/business-balance/discount/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/identity/api/advertisement-image/search").permitAll()
                        .pathMatchers(HttpMethod.GET, "/identity/api/business/*/with-images").permitAll()
                        .pathMatchers(HttpMethod.POST, "/identity/api/leads/capture").permitAll()
                        .pathMatchers(HttpMethod.POST, "/identity/api/2fa/verify-login").permitAll()
                        .pathMatchers(HttpMethod.GET, "/identity/api/2fa/check/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/swagger-ui.html", "/swagger-ui/**", "/v2/api-docs.yaml", "/v3/api-docs.yaml", "/v2/api-docs/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtDecoder(jwtDecoder()))
                )
                .addFilterAt((exchange, chain) -> validateHeader(exchange)
                                .flatMap(valid -> valid ? chain.filter(exchange) : exchange.getResponse().setComplete()),
                        SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    private Mono<Boolean> validateHeader(ServerWebExchange exchange) {
        if (!enableHeaderValidation) {
            return Mono.just(true);
        }

        String path = exchange.getRequest().getPath().value();
        if (isPublicPath(path)) {
            return Mono.just(true);
        }

        boolean isValid = exchange.getRequest().getHeaders().containsKey(REQUIRED_HEADER) &&
                expectedHeaderValue.equals(exchange.getRequest().getHeaders().getFirst(REQUIRED_HEADER));

        if (!isValid) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        }

        return Mono.just(isValid);
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/actuator/") ||
               path.startsWith("/identity/api/auth/") ||
               path.startsWith("/identity/api/business-balance/discount/") ||
               path.equals("/identity/api/advertisement-image/search") ||
               path.startsWith("/identity/api/business/") && path.endsWith("/with-images") ||
               path.equals("/identity/api/leads/capture") ||
               path.equals("/identity/api/2fa/verify-login") ||
               path.startsWith("/identity/api/2fa/check/") ||
               path.contains("/swagger-ui") ||
               path.contains("/api-docs") ||
               path.contains("/swagger-resources") ||
               path.contains("/webjars/");
    }

    @Bean
    public CachingReactiveJwtDecoder jwtDecoder() {
        try {
            return new CachingReactiveJwtDecoder(urlMethodValidation, jwtCacheProperties);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo inicializar el JWT Decoder con cache", e);
        }
    }
}
