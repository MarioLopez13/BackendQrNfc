package com.smartpayut.transaction.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.smartpayut.transaction.event.PaymentEvent;
import com.smartpayut.transaction.event.WalletEvent;
import com.smartpayut.transaction.repository.ProcessedEventRepository;
import com.smartpayut.transaction.repository.TransactionRecordRepository;
import com.smartpayut.transaction.service.EventProjectionService;

@DataJpaTest
@ActiveProfiles("test")
@Import(EventProjectionService.class)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class TransactionProjectionIntegrationTest {

    @Autowired
    private EventProjectionService projectionService;

    @Autowired
    private TransactionRecordRepository transactionRepository;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @Test
    void storesProjectionAndProcessedEventAtomically() {
        UUID paymentId = UUID.randomUUID();
        PaymentEvent event = new PaymentEvent(
                "payment-event-001",
                "payment.completed",
                1,
                OffsetDateTime.now(),
                paymentId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "NFC",
                "COMPLETED",
                new BigDecimal("0.35"),
                "USD",
                "BUS-20",
                "Ruta Universitaria",
                null);

        projectionService.process(event);
        projectionService.process(event);

        assertThat(transactionRepository.findByCorrelationId(paymentId.toString())).isPresent();
        assertThat(processedEventRepository.existsById("payment-event-001")).isTrue();
        assertThat(transactionRepository.count()).isOne();
    }

    @Test
    void paymentEventEnrichesWalletProjectionWithoutCreatingDuplicate() {
        UUID paymentId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        projectionService.process(walletEvent(paymentId, walletId, userId));
        projectionService.process(paymentEvent(
                "payment-after-wallet",
                paymentId,
                walletId,
                userId));

        var record = transactionRepository.findByCorrelationId(paymentId.toString()).orElseThrow();
        assertThat(transactionRepository.count()).isOne();
        assertThat(record.getMethod()).isEqualTo("QR");
        assertThat(record.getTransactionType().name()).isEqualTo("PAYMENT");
        assertThat(record.getStatus().name()).isEqualTo("COMPLETED");
    }

    @Test
    void lateWalletEventDoesNotOverwriteCompletedPaymentProjection() {
        UUID paymentId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        projectionService.process(paymentEvent(
                "payment-before-wallet",
                paymentId,
                walletId,
                userId));
        projectionService.process(walletEvent(paymentId, walletId, userId));

        var record = transactionRepository.findByCorrelationId(paymentId.toString()).orElseThrow();
        assertThat(transactionRepository.count()).isOne();
        assertThat(record.getMethod()).isEqualTo("QR");
        assertThat(record.getTransactionType().name()).isEqualTo("PAYMENT");
        assertThat(record.getStatus().name()).isEqualTo("COMPLETED");
    }

    @Test
    void differentEventIdsForSamePaymentRemainOneProjection() {
        UUID paymentId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        projectionService.process(paymentEvent("original-event", paymentId, walletId, userId));
        projectionService.process(paymentEvent("reconciled-event", paymentId, walletId, userId));

        assertThat(transactionRepository.count()).isOne();
        assertThat(processedEventRepository.count()).isEqualTo(2);
    }

    private PaymentEvent paymentEvent(
            String eventId,
            UUID paymentId,
            UUID walletId,
            UUID userId) {
        return new PaymentEvent(
                eventId,
                "payment.completed",
                1,
                OffsetDateTime.now(),
                paymentId,
                walletId,
                userId,
                "QR",
                "COMPLETED",
                new BigDecimal("3.50"),
                "USD",
                "BUS-20",
                "Ruta Universitaria",
                null);
    }

    private WalletEvent walletEvent(UUID paymentId, UUID walletId, UUID userId) {
        return new WalletEvent(
                UUID.randomUUID(),
                "wallet.debited",
                1,
                OffsetDateTime.now(),
                walletId,
                userId,
                UUID.randomUUID(),
                new BigDecimal("3.50"),
                new BigDecimal("10.00"),
                new BigDecimal("6.50"),
                "USD",
                paymentId.toString(),
                "payment:" + paymentId);
    }
}
