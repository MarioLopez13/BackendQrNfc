package com.smartpayut.wallet.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.authentication.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.*;

@Configuration
public class WalletSecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.resource-id:smartpayut-admin}")
    private String resourceId;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(c -> c.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a.requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/internal/**").hasRole("SERVICE").anyRequest().authenticated())
                .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(roles()))).build();
    }

    private Converter<Jwt, ? extends AbstractAuthenticationToken> roles() {
        return jwt -> {
            Set<String> roles = new HashSet<>();
            extractRoles(jwt.getClaim("realm_access"), roles);
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
