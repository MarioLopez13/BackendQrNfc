package com.smartpayut.identity.controller;

import com.smartpayut.identity.dto.request.*;
import com.smartpayut.identity.dto.response.*;
import com.smartpayut.identity.service.*;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserQueryService queries;
    private final UserService users;

    public UserController(UserQueryService q, UserService u) {
        queries = q;
        users = u;
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success("Usuario autenticado", queries.me(jwt.getSubject()));
    }

    @PostMapping("/search")
    public PaginatedResponse<UserResponse> search(@Valid @RequestBody UserSearchRequest r) {
        return queries.search(r);
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> byId(@PathVariable UUID id) {
        return ApiResponse.success("Usuario encontrado", queries.byId(id));
    }

    @PatchMapping("/{id}")
    public ApiResponse<UserResponse> update(@PathVariable UUID id, @Valid @RequestBody UserUpdateRequest r) {
        return ApiResponse.success("Usuario actualizado", users.update(id, r));
    }
}
