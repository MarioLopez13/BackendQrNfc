package com.kynsoft.gateway.infrastructure.filter;

import com.kynsoft.gateway.infrastructure.security.CachedUserInfo;
import com.kynsoft.gateway.infrastructure.security.CachingReactiveJwtDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Filtro global que extrae el ID del usuario y otros atributos relevantes del token JWT
 * y los añade como encabezados a todas las solicitudes entrantes.
 *
 * Optimizado con cache: Si la informacion del usuario ya esta en cache,
 * se usa directamente sin necesidad de extraer los claims del JWT.
 */
@Component
@Slf4j
public class UserInfoHeaderFilter implements GlobalFilter, Ordered {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_NAME_HEADER = "X-User-Name";
    private static final String USER_EMAIL_HEADER = "X-User-Email";
    private static final String USER_ROLES_HEADER = "X-User-Roles";
    private static final String BEARER_PREFIX = "Bearer ";

    private final CachingReactiveJwtDecoder jwtDecoder;

    public UserInfoHeaderFilter(CachingReactiveJwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Intentar obtener del cache primero
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length());
            String tokenHash = jwtDecoder.getTokenHash(token);

            CachedUserInfo cachedInfo = jwtDecoder.getCachedUserInfo(tokenHash);
            if (cachedInfo != null) {
                // Cache HIT - usar info cacheada directamente
                log.debug("UserInfo cache HIT para token hash: {}...", tokenHash.substring(0, 8));
                ServerHttpRequest request = addCachedUserInfoAsHeaders(exchange.getRequest(), cachedInfo);
                return chain.filter(exchange.mutate().request(request).build());
            }
        }

        // Cache MISS - extraer del JWT y cachear
        return ReactiveSecurityContextHolder.getContext()
                .filter(securityContext -> securityContext.getAuthentication() != null)
                .map(SecurityContext::getAuthentication)
                .filter(auth -> auth instanceof JwtAuthenticationToken)
                .cast(JwtAuthenticationToken.class)
                .map(jwtAuth -> {
                    Jwt jwt = jwtAuth.getToken();

                    // Cachear la info del usuario para proximas peticiones
                    if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
                        String token = authHeader.substring(BEARER_PREFIX.length());
                        String tokenHash = jwtDecoder.getTokenHash(token);
                        jwtDecoder.cacheUserInfo(tokenHash, jwt, jwtAuth.getAuthorities());
                    }

                    ServerHttpRequest request = addJwtClaimsAsHeaders(exchange.getRequest(), jwt, jwtAuth);
                    return exchange.mutate().request(request).build();
                })
                .defaultIfEmpty(exchange)
                .flatMap(chain::filter);
    }

    /**
     * Agrega la informacion del usuario desde el cache como headers HTTP.
     */
    private ServerHttpRequest addCachedUserInfoAsHeaders(ServerHttpRequest request, CachedUserInfo userInfo) {
        ServerHttpRequest.Builder builder = request.mutate();

        if (userInfo.getUserId() != null && !userInfo.getUserId().isEmpty()) {
            builder.header(USER_ID_HEADER, userInfo.getUserId());
        }
        if (userInfo.getUserName() != null && !userInfo.getUserName().isEmpty()) {
            builder.header(USER_NAME_HEADER, userInfo.getUserName());
        }
        if (userInfo.getEmail() != null && !userInfo.getEmail().isEmpty()) {
            builder.header(USER_EMAIL_HEADER, userInfo.getEmail());
        }
        if (userInfo.getRoles() != null && !userInfo.getRoles().isEmpty()) {
            builder.header(USER_ROLES_HEADER, userInfo.getRoles());
        }

        return builder.build();
    }

    /**
     * Extrae los claims del JWT y roles de la autenticacion, y los anade como headers HTTP.
     */
    private ServerHttpRequest addJwtClaimsAsHeaders(ServerHttpRequest request, Jwt jwt, JwtAuthenticationToken auth) {
        ServerHttpRequest.Builder builder = request.mutate();

        addClaimAsHeader(builder, jwt, "sub", USER_ID_HEADER);
        addClaimAsHeader(builder, jwt, "preferred_username", USER_NAME_HEADER);
        addClaimAsHeader(builder, jwt, "email", USER_EMAIL_HEADER);

        if (auth.getAuthorities() != null && !auth.getAuthorities().isEmpty()) {
            try {
                String roles = auth.getAuthorities().stream()
                        .map(authority -> authority.getAuthority())
                        .collect(java.util.stream.Collectors.joining(","));

                if (!roles.isEmpty()) {
                    builder.header(USER_ROLES_HEADER, roles);
                }
            } catch (Exception e) {
                log.warn("Error al procesar roles: {}", e.getMessage());
            }
        }

        return builder.build();
    }

    private void addClaimAsHeader(ServerHttpRequest.Builder builder, Jwt jwt, String claimName, String headerName) {
        try {
            String claimValue = jwt.getClaimAsString(claimName);
            if (claimValue != null && !claimValue.isEmpty()) {
                builder.header(headerName, claimValue);
            }
        } catch (Exception e) {
            log.warn("Error al procesar claim '{}' del token: {}", claimName, e.getMessage());
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }
}