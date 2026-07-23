package com.smartpayut.identity.controller;

import com.smartpayut.identity.dto.request.*;
import com.smartpayut.identity.dto.response.*;
import com.smartpayut.identity.service.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationService authentication;
    private final UserRegistrationService registration;
    private final UserService users;

    public AuthController(AuthenticationService a, UserRegistrationService r, UserService u) {
        authentication = a;
        registration = r;
        users = u;
    }

    @PostMapping("/authenticate")
    public TokenResponse authenticate(@Valid @RequestBody LoginRequest r) {
        return authentication.authenticate(r);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody UserRegistrationRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Usuario registrado", registration.register(r)));
    }

    @PostMapping("/delete-account")
    public ApiResponse<Void> delete(@Valid @RequestBody DeleteAccountRequest r, @AuthenticationPrincipal Jwt jwt) {
        users.delete(r, jwt.getSubject());
        return ApiResponse.success("Cuenta eliminada", null);
    }
}
