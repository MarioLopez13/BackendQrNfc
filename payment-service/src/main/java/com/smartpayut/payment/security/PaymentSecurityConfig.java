package com.smartpayut.payment.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
public class PaymentSecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/info", "/actuator/prometheus").permitAll()
                        .requestMatchers("/api/payments/placetopay/callback").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(roles())))
                .build();
    }

    private Converter<Jwt, ? extends AbstractAuthenticationToken> roles() {
        return jwt -> {
            Set<String> roles = new HashSet<>();
            Object realmAccess = jwt.getClaim("realm_access");
            if (realmAccess instanceof Map<?, ?> claims
                    && claims.get("roles") instanceof Collection<?> realmRoles) {
                realmRoles.stream()
                        .map(Object::toString)
                        .map(String::toUpperCase)
                        .filter(Set.of("ADMIN", "OPERATOR", "USER", "SERVICE")::contains)
                        .map(role -> "ROLE_" + role)
                        .forEach(roles::add);
            }
            return new JwtAuthenticationToken(
                    jwt,
                    roles.stream().map(SimpleGrantedAuthority::new).toList(),
                    jwt.getSubject());
        };
    }
}
