package com.smartpayut.payment.util;

import java.util.UUID;

public final class IdempotencyKeys {

    private IdempotencyKeys() {
    }

    public static String resolve(String suppliedKey, String operation) {
        if (suppliedKey != null && !suppliedKey.isBlank()) {
            String normalized = suppliedKey.trim();
            if (normalized.length() > 150) {
                throw new IllegalArgumentException("Idempotency-Key no puede superar 150 caracteres.");
            }
            return normalized;
        }
        return "legacy:" + operation + ":" + UUID.randomUUID();
    }
}
