package com.smartpayut.transaction.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.smartpayut.transaction.event.PaymentEvent;
import com.smartpayut.transaction.event.WalletEvent;
import com.smartpayut.transaction.mapper.TransactionMapper;
import com.smartpayut.transaction.service.EventProjectionService;
import com.smartpayut.transaction.service.TransactionQueryService;
import com.smartpayut.transaction.validator.PaginationValidator;

@DataJpaTest
@ActiveProfiles("test")
@Import({
        EventProjectionService.class,
        TransactionQueryService.class,
        TransactionMapper.class,
        PaginationValidator.class
})
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class AdminTransactionFilteringIntegrationTest {

    private static final LocalDate JULY_2026 = LocalDate.of(2026, 7, 1);

    @Autowired
    private EventProjectionService projectionService;

    @Autowired
    private TransactionQueryService queryService;

    private UUID qrUserId;
    private UUID qrWalletId;

    @BeforeEach
    void seedTransactions() {
        qrUserId = UUID.randomUUID();
        qrWalletId = UUID.randomUUID();

        projectionService.process(paymentEvent(
                "qr-payment",
                "payment.completed",
                UUID.randomUUID(),
                qrUserId,
                qrWalletId,
                "QR",
                "COMPLETED",
                "BUS-102",
                "Ruta Norte",
                10));
        projectionService.process(paymentEvent(
                "nfc-payment",
                "payment.failed",
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "NFC",
                "FAILED",
                "BUS-205",
                "Ruta Sur",
                20));
        projectionService.process(paymentEvent(
                "top-up",
                "topup.completed",
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "PLACETOPAY",
                "COMPLETED",
                null,
                "Recarga de saldo",
                22));
        projectionService.process(walletCreatedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                5));
    }

    @Test
    void listsAllTransactionsWithoutFilters() {
        var result = queryService.all(0, 20, null, null, null, null, null, null);

        assertThat(result.items()).hasSize(4);
        assertThat(result.totalElements()).isEqualTo(4);
        assertThat(result.page()).isZero();
        assertThat(result.pageSize()).isEqualTo(20);
    }

    @Test
    void appliesIndividualTypeMethodAndStatusFilters() {
        var byType = queryService.all(0, 20, null, "TOP_UP", null, null, null, null);
        var byQrMethod = queryService.all(0, 20, null, null, "QR", null, null, null);
        var byNfcMethod = queryService.all(0, 20, null, null, "NFC", null, null, null);
        var byStatus = queryService.all(0, 20, null, null, null, "FAILED", null, null);

        assertThat(byType.items()).hasSize(1)
                .allMatch(item -> item.type().name().equals("TOP_UP"));
        assertThat(byQrMethod.items()).hasSize(1)
                .allMatch(item -> "QR".equals(item.method()));
        assertThat(byNfcMethod.items()).hasSize(1)
                .allMatch(item -> "NFC".equals(item.method()));
        assertThat(byStatus.items()).hasSize(1)
                .allMatch(item -> item.status().name().equals("FAILED"));
    }

    @Test
    void combinesFiltersBeforePagination() {
        var result = queryService.all(
                0,
                20,
                "102",
                "PAYMENT",
                "QR",
                "COMPLETED",
                JULY_2026,
                LocalDate.of(2026, 7, 24));

        assertThat(result.totalElements()).isOne();
        assertThat(result.items().getFirst().busCode()).isEqualTo("BUS-102");
    }

    @Test
    void searchesAcrossEverySupportedField() {
        var qrTransaction = queryService
                .all(0, 20, "BUS-102", null, null, null, null, null)
                .items()
                .getFirst();

        assertSingleSearchMatch(qrTransaction.id().toString().substring(0, 8));
        assertSingleSearchMatch(qrUserId.toString().substring(0, 8));
        assertSingleSearchMatch(qrWalletId.toString().substring(0, 8));
        assertSingleSearchMatch("102");
        assertSingleSearchMatch("norte");
    }

    @Test
    void filtersInclusiveCalendarDates() {
        var result = queryService.all(
                0,
                20,
                null,
                null,
                null,
                null,
                LocalDate.of(2026, 7, 20),
                LocalDate.of(2026, 7, 20));

        assertThat(result.totalElements()).isOne();
        assertThat(result.items().getFirst().method()).isEqualTo("NFC");
    }

    @Test
    void preservesFilteredPaginationMetadata() {
        var firstPage = queryService.all(
                0,
                1,
                null,
                "PAYMENT",
                null,
                null,
                null,
                null);
        var secondPage = queryService.all(
                1,
                1,
                null,
                "PAYMENT",
                null,
                null,
                null,
                null);

        assertThat(firstPage.items()).hasSize(1);
        assertThat(secondPage.items()).hasSize(1);
        assertThat(firstPage.totalElements()).isEqualTo(2);
        assertThat(firstPage.totalPages()).isEqualTo(2);
    }

    @Test
    void returnsAnEmptyPageWhenNoRecordMatches() {
        var result = queryService.all(
                0,
                20,
                null,
                null,
                null,
                "REFUNDED",
                null,
                null);

        assertThat(result.items()).isEmpty();
        assertThat(result.totalElements()).isZero();
        assertThat(result.totalPages()).isZero();
    }

    @Test
    void rejectsAnInvertedDateRange() {
        assertThatThrownBy(() -> queryService.all(
                0,
                20,
                null,
                null,
                null,
                null,
                LocalDate.of(2026, 7, 24),
                LocalDate.of(2026, 7, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("from no puede ser posterior a to.");
    }

    private void assertSingleSearchMatch(String search) {
        var result = queryService.all(0, 20, search, null, null, null, null, null);

        assertThat(result.totalElements()).isOne();
        assertThat(result.items().getFirst().busCode()).isEqualTo("BUS-102");
    }

    private PaymentEvent paymentEvent(
            String eventId,
            String eventType,
            UUID paymentId,
            UUID userId,
            UUID walletId,
            String method,
            String status,
            String busCode,
            String routeName,
            int day) {
        return new PaymentEvent(
                eventId,
                eventType,
                1,
                OffsetDateTime.of(2026, 7, day, 12, 0, 0, 0, ZoneOffset.UTC),
                paymentId,
                userId,
                walletId,
                method,
                status,
                new BigDecimal("3.50"),
                "USD",
                busCode,
                routeName,
                "FAILED".equals(status) ? "Pago rechazado" : null);
    }

    private WalletEvent walletCreatedEvent(UUID walletId, UUID userId, int day) {
        return new WalletEvent(
                UUID.randomUUID(),
                "wallet.created",
                1,
                OffsetDateTime.of(2026, 7, day, 12, 0, 0, 0, ZoneOffset.UTC),
                walletId,
                userId,
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "USD",
                null,
                null);
    }
}
