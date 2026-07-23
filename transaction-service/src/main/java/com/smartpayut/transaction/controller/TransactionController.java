package com.smartpayut.transaction.controller;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartpayut.transaction.dto.common.ApiResponse;
import com.smartpayut.transaction.dto.response.PageResponse;
import com.smartpayut.transaction.dto.response.TransactionResponse;
import com.smartpayut.transaction.security.CurrentUserIdResolver;
import com.smartpayut.transaction.service.TransactionQueryService;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionQueryService queryService;
    private final CurrentUserIdResolver userIdResolver;

    public TransactionController(TransactionQueryService queryService, CurrentUserIdResolver userIdResolver) {
        this.queryService = queryService;
        this.userIdResolver = userIdResolver;
    }

    @GetMapping({"", "/me"})
    public ApiResponse<PageResponse<TransactionResponse>> mine(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(
                "Historial consultado correctamente.",
                queryService.mine(userIdResolver.resolve(jwt), page, pageSize));
    }

    @GetMapping("/me/{id}")
    public ApiResponse<TransactionResponse> mineById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {
        return ApiResponse.ok(
                "Transacción consultada correctamente.",
                queryService.mineById(userIdResolver.resolve(jwt), id));
    }
}
