package com.smartpayut.notification.service;

import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartpayut.notification.domain.entity.Notification;
import com.smartpayut.notification.domain.entity.ProcessedEvent;
import com.smartpayut.notification.domain.enumeration.NotificationSource;
import com.smartpayut.notification.domain.enumeration.NotificationType;
import com.smartpayut.notification.event.IdentityUserCreatedEvent;
import com.smartpayut.notification.event.PaymentEvent;
import com.smartpayut.notification.event.WalletEvent;
import com.smartpayut.notification.repository.ProcessedEventRepository;
import com.smartpayut.notification.service.NotificationMessageFactory.MessageContent;

@Service
public class NotificationProjectionService {

    private static final Set<String> WALLET_EVENTS = Set.of(
            "wallet.created", "wallet.credited", "wallet.debited", "wallet.refunded");
    private static final Set<String> PAYMENT_EVENTS = Set.of(
            "payment.completed", "payment.failed", "payment.refunded",
            "topup.completed", "topup.failed");

    private final ProcessedEventRepository processedEventRepository;
    private final NotificationMessageFactory messageFactory;
    private final NotificationSender sender;

    public NotificationProjectionService(
            ProcessedEventRepository processedEventRepository,
            NotificationMessageFactory messageFactory,
            NotificationSender sender) {
        this.processedEventRepository = processedEventRepository;
        this.messageFactory = messageFactory;
        this.sender = sender;
    }

    @Transactional
    public void process(IdentityUserCreatedEvent event) {
        required(event.eventId(), "eventId");
        required(event.userId(), "userId");
        if (!"identity.user.created".equals(event.eventType()) || event.eventVersion() != 1) {
            throw new IllegalArgumentException("Evento Identity no soportado.");
        }
        String eventId = event.eventId().toString();
        if (processedEventRepository.existsById(eventId)) {
            return;
        }
        sender.send(new Notification(
                eventId,
                event.userId(),
                NotificationType.WELCOME,
                "Bienvenido a SmartPayUT",
                "Tu cuenta fue creada correctamente.",
                NotificationSource.IDENTITY,
                event.userId().toString(),
                null));
        processedEventRepository.save(new ProcessedEvent(eventId, event.eventType()));
    }

    @Transactional
    public void process(WalletEvent event) {
        validateWallet(event);
        String eventId = event.eventId().toString();
        if (processedEventRepository.existsById(eventId)) {
            return;
        }
        MessageContent content = messageFactory.forWallet(event);
        String reference = event.referenceId() != null
                ? event.referenceId()
                : optionalUuid(event.movementId(), event.walletId());
        sender.send(new Notification(
                eventId, event.userId(), messageFactory.type(event.eventType()),
                content.title(), content.message(), NotificationSource.WALLET,
                reference, event.amount()));
        processedEventRepository.save(new ProcessedEvent(eventId, event.eventType()));
    }

    @Transactional
    public void process(PaymentEvent event) {
        validatePayment(event);
        if (processedEventRepository.existsById(event.eventId())) {
            return;
        }
        MessageContent content = messageFactory.forPayment(event);
        sender.send(new Notification(
                event.eventId(), event.userId(), messageFactory.type(event.eventType()),
                content.title(), content.message(), NotificationSource.PAYMENT,
                event.paymentId().toString(), event.amount()));
        processedEventRepository.save(new ProcessedEvent(event.eventId(), event.eventType()));
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
}
