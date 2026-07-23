package com.smartpayut.transaction.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.smartpayut.transaction.domain.entity.TransactionRecord;
import com.smartpayut.transaction.domain.enumeration.TransactionStatus;
import com.smartpayut.transaction.domain.enumeration.TransactionType;
import com.smartpayut.transaction.repository.TransactionRecordRepository;
import com.smartpayut.transaction.service.DashboardQueryService;

@DataJpaTest
@ActiveProfiles("test")
@Import(DashboardQueryService.class)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class DashboardSummaryIntegrationTest {

    @Autowired
    private TransactionRecordRepository repository;

    @Autowired
    private DashboardQueryService dashboardQueryService;

    @Test
    void calculatesAggregatesAndIncludesDaysWithoutOperations() {
        LocalDate today = OffsetDateTime.now(ZoneOffset.UTC).toLocalDate();
        store(TransactionStatus.COMPLETED, "QR", "10.50", today.atTime(10, 0).atOffset(ZoneOffset.UTC));
        store(TransactionStatus.PENDING, "QR", "8.00", today.atTime(11, 0).atOffset(ZoneOffset.UTC));
        store(TransactionStatus.FAILED, "NFC", "3.00",
                today.minusDays(1).atTime(12, 0).atOffset(ZoneOffset.UTC));
        store(TransactionStatus.REFUNDED, "NFC", "2.00",
                today.minusDays(3).atTime(13, 0).atOffset(ZoneOffset.UTC));

        var summary = dashboardQueryService.summary(4);

        assertThat(summary.totalTransactions()).isEqualTo(4);
        assertThat(summary.completedTransactions()).isEqualTo(1);
        assertThat(summary.pendingTransactions()).isEqualTo(1);
        assertThat(summary.failedTransactions()).isEqualTo(1);
        assertThat(summary.refundedTransactions()).isEqualTo(1);
        assertThat(summary.approvedAmount()).isEqualByComparingTo("10.50");
        assertThat(summary.operationsByMethod()).containsEntry("QR", 2L).containsEntry("NFC", 2L);
        assertThat(summary.dailyOperations()).hasSize(4);
        assertThat(summary.dailyOperations())
                .anySatisfy(day -> {
                    assertThat(day.date()).isEqualTo(today.minusDays(2));
                    assertThat(day.count()).isZero();
                });
        assertThat(summary.dailyOperations())
                .extracting(day -> day.date())
                .isSorted();
    }

    @Test
    void rejectsDaysOutsideAllowedRange() {
        assertThatThrownBy(() -> dashboardQueryService.summary(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("days debe estar entre 1 y 90.");
        assertThatThrownBy(() -> dashboardQueryService.summary(91))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("days debe estar entre 1 y 90.");
    }

    private void store(
            TransactionStatus status,
            String method,
            String amount,
            OffsetDateTime occurredAt) {
        UUID paymentId = UUID.randomUUID();
        TransactionRecord record = new TransactionRecord(
                paymentId.toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID());
        record.applyPayment(
                UUID.randomUUID().toString(),
                paymentId,
                UUID.randomUUID(),
                TransactionType.PAYMENT,
                method,
                status,
                new BigDecimal(amount),
                "USD",
                "BUS-01",
                "Ruta Universitaria",
                status == TransactionStatus.FAILED ? "Pago rechazado" : null,
                occurredAt);
        repository.saveAndFlush(record);
    }
}
