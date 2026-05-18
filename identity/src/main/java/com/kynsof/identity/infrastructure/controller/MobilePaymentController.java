package com.kynsof.identity.infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/mobile-payments")
public class MobilePaymentController {

    private static final BigDecimal INITIAL_BALANCE = BigDecimal.valueOf(100.00);

    private static final Map<String, BigDecimal> userBalances = new HashMap<>();
    private static final Map<String, Map<String, Object>> transactions = new LinkedHashMap<>();

    @PostMapping("/qr")
    public ResponseEntity<?> processQrPayment(
            @RequestHeader(value = "X-User-ID", required = false) String userId,
            @RequestBody Map<String, Object> payload
    ) {
        return processPayment(userId, payload, "QR", "Pago procesado correctamente.");
    }

    @PostMapping("/nfc")
    public ResponseEntity<?> processNfcPayment(
            @RequestHeader(value = "X-User-ID", required = false) String userId,
            @RequestBody Map<String, Object> payload
    ) {
        return processPayment(userId, payload, "NFC", "Pago NFC procesado correctamente.");
    }

    @GetMapping
    public ResponseEntity<?> getAllTransactions() {
        List<Map<String, Object>> data = new ArrayList<>(transactions.values());
        Collections.reverse(data);

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "Transacciones consultadas correctamente.",
                        "data", data
                )
        );
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<?> getTransactionById(@PathVariable String transactionId) {
        Map<String, Object> transaction = transactions.get(transactionId);

        if (transaction == null) {
            return ResponseEntity.status(404).body(
                    Map.of(
                            "success", false,
                            "message", "Transacción no encontrada."
                    )
            );
        }

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "Transacción consultada correctamente.",
                        "data", transaction
                )
        );
    }

    private ResponseEntity<?> processPayment(
            String userId,
            Map<String, Object> payload,
            String method,
            String successMessage
    ) {
        String safeUserId = getSafeUserId(userId);
        BigDecimal amount = extractAmount(payload.get("amount"));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "message", "El monto del pago debe ser mayor a cero."
                    )
            );
        }

        BigDecimal currentBalance = userBalances.getOrDefault(safeUserId, INITIAL_BALANCE);

        if (currentBalance.compareTo(amount) < 0) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "message", "Saldo insuficiente.",
                            "data", Map.of(
                                    "currentBalance", currentBalance,
                                    "amount", amount
                            )
                    )
            );
        }

        BigDecimal updatedBalance = currentBalance.subtract(amount);
        userBalances.put(safeUserId, updatedBalance);

        String transactionId = UUID.randomUUID().toString();

        Map<String, Object> transaction = new LinkedHashMap<>();
        transaction.put("transactionId", transactionId);
        transaction.put("id", transactionId);
        transaction.put("method", method);
        transaction.put("status", "Completado");
        transaction.put("processedAt", LocalDateTime.now().toString());
        transaction.put("userId", safeUserId);
        transaction.put("busCode", payload.getOrDefault("busCode", "BUS-DEMO"));
        transaction.put("routeName", payload.getOrDefault("routeName", "Ruta demo"));
        transaction.put("amount", amount);
        transaction.put("previousBalance", currentBalance);
        transaction.put("updatedBalance", updatedBalance);

        transactions.put(transactionId, transaction);

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", successMessage,
                        "data", transaction
                )
        );
    }

    private String getSafeUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return "demo-user";
        }

        return userId;
    }
    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(
            @RequestHeader(value = "X-User-ID", required = false) String userId
    ) {
        String safeUserId = getSafeUserId(userId);

        BigDecimal currentBalance =
                userBalances.getOrDefault(safeUserId, INITIAL_BALANCE);

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "Saldo consultado correctamente.",
                        "data", Map.of(
                                "userId", safeUserId,
                                "balance", currentBalance
                        )
                )
        );
    }
    private BigDecimal extractAmount(Object rawAmount) {
        if (rawAmount == null) {
            return BigDecimal.ZERO;
        }

        try {
            return new BigDecimal(rawAmount.toString());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}