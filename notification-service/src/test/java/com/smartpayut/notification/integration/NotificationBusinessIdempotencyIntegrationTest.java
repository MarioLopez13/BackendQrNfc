package com.smartpayut.notification.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.smartpayut.notification.event.PaymentEvent;
import com.smartpayut.notification.event.WalletEvent;
import com.smartpayut.notification.repository.NotificationRepository;
import com.smartpayut.notification.repository.ProcessedEventRepository;
import com.smartpayut.notification.service.InAppNotificationSender;
import com.smartpayut.notification.service.NotificationMessageFactory;
import com.smartpayut.notification.service.NotificationPersistenceService;
import com.smartpayut.notification.service.NotificationProjectionService;
import com.smartpayut.notification.service.ProcessedEventRecorder;

@DataJpaTest
@ActiveProfiles("test")
@Import({
        NotificationProjectionService.class,
        NotificationPersistenceService.class,
        ProcessedEventRecorder.class,
        NotificationMessageFactory.class,
        InAppNotificationSender.class
})
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class NotificationBusinessIdempotencyIntegrationTest {

    @Autowired
    private NotificationProjectionService projectionService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @BeforeEach
    void cleanDatabase() {
        processedEventRepository.deleteAll();
        notificationRepository.deleteAll();
    }

    @Test
    void sameEventIdCreatesOneNotification() {
        UUID paymentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        PaymentEvent event = payment("same-event", "payment.completed", paymentId, userId);

        projectionService.process(event);
        projectionService.process(event);

        assertThat(notificationRepository.count()).isOne();
        assertThat(processedEventRepository.count()).isOne();
    }

    @Test
    void differentEventIdsForSamePaymentAndTypeCreateOneNotification() {
        UUID paymentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        projectionService.process(payment("original-event", "payment.completed", paymentId, userId));
        projectionService.process(payment("reconciled-event", "payment.completed", paymentId, userId));

        assertThat(notificationRepository.count()).isOne();
        assertThat(processedEventRepository.count()).isEqualTo(2);
    }

    @Test
    void completedAndRefundedForSamePaymentCreateTwoNotifications() {
        UUID paymentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        projectionService.process(payment("completed-event", "payment.completed", paymentId, userId));
        projectionService.process(payment("refunded-event", "payment.refunded", paymentId, userId));

        assertThat(notificationRepository.count()).isEqualTo(2);
    }

    @Test
    void differentPaymentsForSameUserCreateTwoNotifications() {
        UUID userId = UUID.randomUUID();

        projectionService.process(payment(
                "first-payment-event",
                "payment.completed",
                UUID.randomUUID(),
                userId));
        projectionService.process(payment(
                "second-payment-event",
                "payment.completed",
                UUID.randomUUID(),
                userId));

        assertThat(notificationRepository.count()).isEqualTo(2);
    }

    @Test
    void concurrentDeliveryCreatesOneNotification() throws Exception {
        UUID paymentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> processConcurrently(
                    payment("concurrent-1", "payment.completed", paymentId, userId),
                    ready,
                    start));
            var second = executor.submit(() -> processConcurrently(
                    payment("concurrent-2", "payment.completed", paymentId, userId),
                    ready,
                    start));

            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            first.get(10, TimeUnit.SECONDS);
            second.get(10, TimeUnit.SECONDS);
        }

        assertThat(notificationRepository.count()).isOne();
        assertThat(processedEventRepository.count()).isEqualTo(2);
    }

    @Test
    void walletEventWithoutReferenceUsesAvailableMovementCorrelation() {
        UUID walletId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID movementId = UUID.randomUUID();

        projectionService.process(wallet(UUID.randomUUID(), walletId, userId, movementId));
        projectionService.process(wallet(UUID.randomUUID(), walletId, userId, movementId));

        assertThat(notificationRepository.count()).isOne();
        assertThat(processedEventRepository.count()).isEqualTo(2);
    }

    private void processConcurrently(
            PaymentEvent event,
            CountDownLatch ready,
            CountDownLatch start) {
        try {
            ready.countDown();
            start.await(5, TimeUnit.SECONDS);
            projectionService.process(event);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }

    private PaymentEvent payment(
            String eventId,
            String eventType,
            UUID paymentId,
            UUID userId) {
        return new PaymentEvent(
                eventId,
                eventType,
                1,
                OffsetDateTime.now(),
                paymentId,
                userId,
                UUID.randomUUID(),
                "QR",
                "COMPLETED",
                new BigDecimal("3.50"),
                "USD",
                "BUS-1",
                "Ruta",
                null);
    }

    private WalletEvent wallet(
            UUID eventId,
            UUID walletId,
            UUID userId,
            UUID movementId) {
        return new WalletEvent(
                eventId,
                "wallet.credited",
                1,
                OffsetDateTime.now(),
                walletId,
                userId,
                movementId,
                new BigDecimal("10.00"),
                BigDecimal.ZERO,
                new BigDecimal("10.00"),
                "USD",
                null,
                "wallet-credit");
    }
}
