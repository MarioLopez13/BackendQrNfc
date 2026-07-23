package com.smartpayut.wallet.security;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.authentication.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import java.util.*;

@Configuration
public class WalletSecurityConfig {
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
            Set<String> r = new HashSet<>();
            Object c = jwt.getClaim("realm_access");
            if (c instanceof Map<?, ?> m && m.get("roles") instanceof Collection<?> v)
                v.stream().map(Object::toString).map(String::toUpperCase)
                        .filter(Set.of("ADMIN", "OPERATOR", "USER", "SERVICE")::contains).map(x -> "ROLE_" + x)
                        .forEach(r::add);
            return new JwtAuthenticationToken(jwt,
                    r.stream().map(org.springframework.security.core.authority.SimpleGrantedAuthority::new).toList(),
                    jwt.getSubject());
        };
    }
}
