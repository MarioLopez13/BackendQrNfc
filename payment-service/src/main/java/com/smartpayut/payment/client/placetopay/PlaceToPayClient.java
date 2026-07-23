package com.smartpayut.payment.client.placetopay;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public interface PlaceToPayClient {

    SessionResult createSession(SessionCommand command);

    SessionStatus querySession(long requestId);

    boolean isValidCallback(long requestId, String status, OffsetDateTime date, String signature);

    record SessionCommand(
            String reference,
            String description,
            BigDecimal amount,
            int expirationMinutes,
            String returnUrl,
            String cancelUrl,
            String ipAddress,
            String userAgent) {
    }

    record SessionResult(long requestId, String processUrl, String status, String message) {
    }

    record SessionStatus(String status, String message) {

        public boolean approved() {
            return "APPROVED".equalsIgnoreCase(status);
        }

        public boolean rejected() {
            return "REJECTED".equalsIgnoreCase(status) || "FAILED".equalsIgnoreCase(status);
        }
    }
}
