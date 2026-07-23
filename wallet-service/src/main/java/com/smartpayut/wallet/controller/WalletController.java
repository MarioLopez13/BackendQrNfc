package com.smartpayut.wallet.controller;

import com.smartpayut.wallet.dto.response.*;
import com.smartpayut.wallet.service.WalletQueryService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallets/me")
public class WalletController {
    private final WalletQueryService queries;

    public WalletController(WalletQueryService q) {
        queries = q;
    }

    @GetMapping
    public ApiResponse<WalletResponse> me(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok("Billetera consultada correctamente.", queries.me(jwt.getSubject()));
    }

    @GetMapping("/balance")
    public ApiResponse<BalanceResponse> balance(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok("Saldo consultado correctamente.", queries.balance(jwt.getSubject()));
    }

    @GetMapping("/movements")
    public ApiResponse<PageResponse<MovementResponse>> movements(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok("Movimientos consultados correctamente.",
                queries.movements(jwt.getSubject(), page, pageSize));
    }
}
