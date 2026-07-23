package com.smartpayut.wallet.controller;

import com.smartpayut.wallet.dto.request.BalanceOperationRequest;
import com.smartpayut.wallet.dto.response.*;
import com.smartpayut.wallet.service.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/internal/wallets")
public class InternalWalletController {
    private final WalletMovementService movements;
    private final WalletQueryService queries;

    public InternalWalletController(WalletMovementService m, WalletQueryService q) {
        movements = m;
        queries = q;
    }

    @PostMapping("/debit")
    public ApiResponse<MovementResponse> debit(@Valid @RequestBody BalanceOperationRequest r) {
        return ApiResponse.ok("Débito procesado.", movements.debit(r));
    }

    @PostMapping("/credit")
    public ApiResponse<MovementResponse> credit(@Valid @RequestBody BalanceOperationRequest r) {
        return ApiResponse.ok("Crédito procesado.", movements.credit(r));
    }

    @PostMapping("/refund")
    public ApiResponse<MovementResponse> refund(@Valid @RequestBody BalanceOperationRequest r) {
        return ApiResponse.ok("Reembolso procesado.", movements.refund(r));
    }

    @GetMapping("/users/{userId}")
    public ApiResponse<WalletResponse> byUser(@PathVariable UUID userId) {
        return ApiResponse.ok("Billetera consultada.", queries.byUser(userId));
    }
}
