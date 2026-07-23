package com.smartpayut.wallet.controller;

import com.smartpayut.wallet.dto.response.*;
import com.smartpayut.wallet.service.WalletQueryService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/mobile-payments")
public class LegacyWalletCompatibilityController {
    private final WalletQueryService queries;

    public LegacyWalletCompatibilityController(WalletQueryService q) {
        queries = q;
    }

    @GetMapping("/balance")
    public ApiResponse<BalanceResponse> balance(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok("Saldo consultado correctamente.", queries.balance(jwt.getSubject()));
    }

    @GetMapping
    public ApiResponse<List<MovementResponse>> movements(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok("Transacciones consultadas correctamente.",
                queries.movements(jwt.getSubject(), 0, 200).items());
    }
}
