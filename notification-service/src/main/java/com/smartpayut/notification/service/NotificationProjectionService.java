package com.smartpayut.notification.service;

import java.util.Set;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.smartpayut.notification.domain.entity.Notification;
import com.smartpayut.notification.domain.enumeration.NotificationSource;
import com.smartpayut.notification.domain.enumeration.NotificationType;
import com.smartpayut.notification.event.IdentityUserCreatedEvent;
import com.smartpayut.notification.event.PaymentEvent;
import com.smartpayut.notification.event.WalletEvent;
import com.smartpayut.notification.service.NotificationMessageFactory.MessageContent;

@Service
public class NotificationProjectionService {

    private static final Set<String> WALLET_EVENTS = Set.of(
            "wallet.created", "wallet.credited", "wallet.debited", "wallet.refunded");
    private static final Set<String> PAYMENT_EVENTS = Set.of(
            "payment.completed", "payment.failed", "payment.refunded",
            "topup.completed", "topup.failed");

    private final NotificationMessageFactory messageFactory;
    private final NotificationPersistenceService persistenceService;
    private final ProcessedEventRecorder processedEventRecorder;

    public NotificationProjectionService(
            NotificationMessageFactory messageFactory,
            NotificationPersistenceService persistenceService,
            ProcessedEventRecorder processedEventRecorder) {
        this.messageFactory = messageFactory;
        this.persistenceService = persistenceService;
        this.processedEventRecorder = processedEventRecorder;
    }

    public void process(IdentityUserCreatedEvent event) {
        required(event.eventId(), "eventId");
        required(event.userId(), "userId");
        if (!"identity.user.created".equals(event.eventType()) || event.eventVersion() != 1) {
            throw new IllegalArgumentException("Evento Identity no soportado.");
        }
        String eventId = event.eventId().toString();
        persist(new Notification(
                eventId,
                event.userId(),
                NotificationType.WELCOME,
                "Bienvenido a SmartPayUT",
                "Tu cuenta fue creada correctamente.",
                NotificationSource.IDENTITY,
                event.userId().toString(),
                null), event.eventType());
    }

    public void process(WalletEvent event) {
        validateWallet(event);
        String eventId = event.eventId().toString();
        if (isTopUpWalletCredit(event)) {
            processedEventRecorder.recordIgnored(eventId, event.eventType());
            return;
        }
        MessageContent content = messageFactory.forWallet(event);
        String reference = event.referenceId() != null
                ? event.referenceId()
                : optionalUuid(event.movementId(), event.walletId());
        persist(new Notification(
                eventId, event.userId(), messageFactory.type(event.eventType()),
                content.title(), content.message(), NotificationSource.WALLET,
                reference, event.amount()), event.eventType());
    }

    public void process(PaymentEvent event) {
        validatePayment(event);
        MessageContent content = messageFactory.forPayment(event);
        persist(new Notification(
                event.eventId(), event.userId(), messageFactory.type(event.eventType()),
                content.title(), content.message(), NotificationSource.PAYMENT,
                event.paymentId().toString(), event.amount()), event.eventType());
    }

    private void validateWallet(WalletEvent event) {
        required(event.eventId(), "eventId");
        required(event.userId(), "userId");
        required(event.walletId(), "walletId");
        if (event.eventType() == null || !WALLET_EVENTS.contains(event.eventType())) {
            throw new IllegalArgumentException("Evento Wallet no soportado.");
        }
    }

    private void validatePayment(PaymentEvent event) {
        if (event.eventId() == null || event.eventId().isBlank()) {
            throw new IllegalArgumentException("eventId es obligatorio.");
        }
        required(event.userId(), "userId");
        required(event.paymentId(), "paymentId");
        if (event.eventType() == null || !PAYMENT_EVENTS.contains(event.eventType())) {
            throw new IllegalArgumentException("Evento Payment no soportado.");
        }
    }

    private boolean isTopUpWalletCredit(WalletEvent event) {
        return "wallet.credited".equals(event.eventType())
                && event.idempotencyKey() != null
                && event.idempotencyKey().startsWith("topup:");
    }

    private <T> T required(T value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " es obligatorio.");
        }
        return value;
    }

    private String optionalUuid(UUID first, UUID second) {
        UUID value = first == null ? second : first;
        return value == null ? null : value.toString();
    }

    private void persist(Notification notification, String eventType) {
        try {
            persistenceService.persist(notification, eventType);
        } catch (DataIntegrityViolationException exception) {
            boolean duplicate = processedEventRecorder.recordDuplicate(
                    notification.getEventId(),
                    eventType,
                    notification.getBusinessKey());
            if (!duplicate) {
                throw exception;
            }
        }
    }
}
