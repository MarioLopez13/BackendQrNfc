package com.smartpayut.notification.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.smartpayut.notification.event.PaymentEvent;
import com.smartpayut.notification.event.WalletEvent;

class NotificationMessageFactoryTest {

    private final NotificationMessageFactory factory = new NotificationMessageFactory();

    @ParameterizedTest
    @MethodSource("paymentTypes")
    void createsSafePaymentMessages(String eventType, String expectedTitle) {
        PaymentEvent event = new PaymentEvent("event", eventType, 1, OffsetDateTime.now(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "QR", "COMPLETED",
                new BigDecimal("0.35"), "USD", null, null, "technical stack trace");
        NotificationMessageFactory.MessageContent content = factory.forPayment(event);
        assertThat(content.title()).isEqualTo(expectedTitle);
        assertThat(content.message()).doesNotContain("technical stack trace", "null");
    }

    @ParameterizedTest
    @MethodSource("walletTypes")
    void createsWalletMessages(String eventType, String expectedTitle) {
        WalletEvent event = new WalletEvent(UUID.randomUUID(), eventType, 1, OffsetDateTime.now(),
                UUID.randomUUID(), UUID.randomUUID(), null, null, null, null, "USD", null, null);
        NotificationMessageFactory.MessageContent content = factory.forWallet(event);
        assertThat(content.title()).isEqualTo(expectedTitle);
        assertThat(content.message()).doesNotContain("null");
    }

    @Test
    void omitsAbsentOptionalAmountAndMethod() {
        PaymentEvent event = new PaymentEvent("event", "payment.failed", 1, OffsetDateTime.now(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null, "FAILED",
                null, "USD", null, null, null);
        assertThat(factory.forPayment(event).message()).isEqualTo("No fue posible completar tu pago.");
    }

    static Stream<Arguments> paymentTypes() {
        return Stream.of(
                Arguments.of("payment.completed", "Pago completado"),
                Arguments.of("payment.failed", "Pago no completado"),
                Arguments.of("payment.refunded", "Pago reembolsado"),
                Arguments.of("topup.completed", "Recarga completada"),
                Arguments.of("topup.failed", "Recarga no completada"));
    }

    static Stream<Arguments> walletTypes() {
        return Stream.of(
                Arguments.of("wallet.created", "Billetera creada"),
                Arguments.of("wallet.credited", "Fondos acreditados"),
                Arguments.of("wallet.debited", "Débito realizado"),
                Arguments.of("wallet.refunded", "Reembolso acreditado"));
    }
}
