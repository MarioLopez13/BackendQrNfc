package com.smartpayut.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.smartpayut.notification.domain.entity.Notification;
import com.smartpayut.notification.domain.entity.ProcessedEvent;
import com.smartpayut.notification.domain.enumeration.NotificationType;
import com.smartpayut.notification.event.IdentityUserCreatedEvent;
import com.smartpayut.notification.event.PaymentEvent;
import com.smartpayut.notification.event.WalletEvent;
import com.smartpayut.notification.repository.ProcessedEventRepository;

@ExtendWith(MockitoExtension.class)
class NotificationProjectionServiceTest {

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @Mock
    private NotificationSender sender;

    private NotificationProjectionService service;

    @BeforeEach
    void setUp() {
        service = new NotificationProjectionService(
                processedEventRepository, new NotificationMessageFactory(), sender);
    }

    @Test
    void createsNotificationForCompletedPayment() {
        PaymentEvent event = payment("payment.completed", new BigDecimal("0.35"));
        service.process(event);
        assertSent(NotificationType.PAYMENT_COMPLETED);
    }

    @Test
    void createsNotificationForFailedPayment() {
        PaymentEvent event = payment("payment.failed", new BigDecimal("0.35"));
        service.process(event);
        assertSent(NotificationType.PAYMENT_FAILED);
    }

    @Test
    void createsNotificationForWalletCredit() {
        WalletEvent event = wallet("wallet.credited", new BigDecimal("10.00"));
        service.process(event);
        assertSent(NotificationType.WALLET_CREDITED);
    }

    @Test
    void createsWelcomeNotificationForCreatedUser() {
        UUID userId = UUID.randomUUID();
        IdentityUserCreatedEvent event = new IdentityUserCreatedEvent(
                UUID.randomUUID(),
                "identity.user.created",
                1,
                OffsetDateTime.now(),
                userId,
                UUID.randomUUID(),
                "user",
                "user@example.com",
                "Test",
                "User",
                "ACTIVE");

        service.process(event);

        assertSent(NotificationType.WELCOME);
    }

    @Test
    void ignoresDuplicateEvent() {
        PaymentEvent event = payment("payment.completed", BigDecimal.ONE);
        when(processedEventRepository.existsById(event.eventId())).thenReturn(true);
        service.process(event);
        verify(sender, never()).send(any());
        verify(processedEventRepository, never()).save(any());
    }

    @Test
    void savesNotificationAndProcessedEvent() {
        service.process(payment("topup.completed", new BigDecimal("20.00")));
        verify(sender).send(any(Notification.class));
        verify(processedEventRepository).save(any(ProcessedEvent.class));
    }

    @Test
    void doesNotRegisterEventWhenNotificationPersistenceFails() {
        PaymentEvent event = payment("payment.completed", BigDecimal.ONE);
        when(sender.send(any())).thenThrow(new IllegalStateException("database failure"));
        assertThatThrownBy(() -> service.process(event)).isInstanceOf(IllegalStateException.class);
        verify(processedEventRepository, never()).save(any());
    }

    @Test
    void acceptsWalletEventWithoutOptionalAmount() {
        service.process(wallet("wallet.created", null));
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(sender).send(captor.capture());
        assertThat(captor.getValue().getAmount()).isNull();
    }

    private void assertSent(NotificationType expectedType) {
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(sender).send(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(expectedType);
    }

    private PaymentEvent payment(String type, BigDecimal amount) {
        return new PaymentEvent(UUID.randomUUID().toString(), type, 1, OffsetDateTime.now(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "QR", "COMPLETED",
                amount, "USD", "BUS-1", "Ruta", null);
    }

    private WalletEvent wallet(String type, BigDecimal amount) {
        return new WalletEvent(UUID.randomUUID(), type, 1, OffsetDateTime.now(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), amount,
                BigDecimal.ZERO, amount, "USD", null, "key");
    }
}
