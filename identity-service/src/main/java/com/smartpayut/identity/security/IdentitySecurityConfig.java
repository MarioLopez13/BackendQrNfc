package com.smartpayut.identity.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.*;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.*;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class IdentitySecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.resource-id:smartpayut-admin}")
    private String resourceId;
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/authenticate", "/api/auth/register", "/actuator/health",
                                "/actuator/info")
                        .permitAll().requestMatchers("/api/users/me", "/api/auth/delete-account").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/*").hasRole("ADMIN")
                        .requestMatchers("/api/users/search", "/api/users/*").hasAnyRole("ADMIN", "OPERATOR")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakRoles())))
                .build();
    }

    private Converter<Jwt, ? extends AbstractAuthenticationToken> keycloakRoles() {
        return jwt -> {
            Set<String> roles = new HashSet<>();
            // realm_access.roles
            extractRoles(jwt.getClaim("realm_access"), roles);
            // resource_access.<resourceId>.roles
            Object resourceAccess = jwt.getClaim("resource_access");
            if (resourceAccess instanceof Map<?, ?> clients
                    && clients.get(resourceId) instanceof Map<?, ?> clientAccess) {
                extractRoles(clientAccess, roles);
            }
            return new JwtAuthenticationToken(jwt,
                    roles.stream().map(SimpleGrantedAuthority::new).toList(),
                    jwt.getSubject());
        };
    }

    private void extractRoles(Object accessClaim, Set<String> target) {
        if (accessClaim instanceof Map<?, ?> map
                && map.get("roles") instanceof Collection<?> values) {
            values.stream()
                    .map(Object::toString)
                    .map(String::toUpperCase)
                    .filter(Set.of("ADMIN", "OPERATOR", "USER", "SERVICE")::contains)
                    .map(role -> "ROLE_" + role)
                    .forEach(target::add);
        }
    }
}
