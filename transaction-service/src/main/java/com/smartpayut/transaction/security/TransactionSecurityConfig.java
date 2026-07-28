package com.smartpayut.transaction.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class TransactionSecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.resource-id:smartpayut-admin}")
    private String resourceId;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/info", "/actuator/prometheus").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(roles())))
                .build();
    }

    private Converter<Jwt, ? extends AbstractAuthenticationToken> roles() {
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
            return new JwtAuthenticationToken(
                    jwt,
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
