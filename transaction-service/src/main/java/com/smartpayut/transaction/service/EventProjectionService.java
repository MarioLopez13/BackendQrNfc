package com.smartpayut.transaction.service;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartpayut.transaction.domain.entity.ProcessedEvent;
import com.smartpayut.transaction.domain.entity.TransactionRecord;
import com.smartpayut.transaction.domain.enumeration.TransactionStatus;
import com.smartpayut.transaction.domain.enumeration.TransactionType;
import com.smartpayut.transaction.event.PaymentEvent;
import com.smartpayut.transaction.event.WalletEvent;
import com.smartpayut.transaction.repository.ProcessedEventRepository;
import com.smartpayut.transaction.repository.TransactionRecordRepository;

@Service
public class EventProjectionService {

    private final TransactionRecordRepository transactionRepository;
    private final ProcessedEventRepository processedEventRepository;

    public EventProjectionService(
            TransactionRecordRepository transactionRepository,
            ProcessedEventRepository processedEventRepository) {
        this.transactionRepository = transactionRepository;
        this.processedEventRepository = processedEventRepository;
    }

    @Transactional
    public void process(WalletEvent event) {
        String eventId = required(event.eventId(), "eventId").toString();
        if (processedEventRepository.existsById(eventId)) {
            return;
        }
        validateWalletEvent(event);
        String correlationId = walletCorrelation(event);
        TransactionRecord record = transactionRepository.findByCorrelationId(correlationId)
                .orElseGet(() -> new TransactionRecord(correlationId, eventId, event.userId()));
        record.applyWallet(
                eventId,
                event.movementId(),
                event.walletId(),
                walletType(event.eventType()),
                event.amount(),
                event.balanceBefore(),
                event.balanceAfter(),
                event.currency(),
                required(event.occurredAt(), "occurredAt"));
        transactionRepository.save(record);
        processedEventRepository.save(new ProcessedEvent(eventId, event.eventType(), "WALLET"));
    }

    @Transactional
    public void process(PaymentEvent event) {
        String eventId = requiredText(event.eventId(), "eventId");
        if (processedEventRepository.existsById(eventId)) {
            return;
        }
        validatePaymentEvent(event);
        String correlationId = event.paymentId().toString();
        TransactionRecord record = transactionRepository.findByCorrelationId(correlationId)
                .orElseGet(() -> new TransactionRecord(correlationId, eventId, event.userId()));
        record.applyPayment(
                eventId,
                event.paymentId(),
                event.walletId(),
                paymentType(event.eventType()),
                event.method(),
                paymentStatus(event.eventType(), event.status()),
                event.amount(),
                event.currency(),
                event.busCode(),
                event.routeName(),
                event.failureReason(),
                event.occurredAt());
        transactionRepository.save(record);
        processedEventRepository.save(new ProcessedEvent(eventId, event.eventType(), "PAYMENT"));
    }

    private void validateWalletEvent(WalletEvent event) {
        requiredText(event.eventType(), "eventType");
        required(event.userId(), "userId");
        required(event.walletId(), "walletId");
        if (!com.smartpayut.transaction.messaging.config.TransactionRabbitConfig.WALLET_ROUTING_KEYS
                .contains(event.eventType())) {
            throw new IllegalArgumentException("Evento Wallet no soportado: " + event.eventType());
        }
    }

    private void validatePaymentEvent(PaymentEvent event) {
        requiredText(event.eventType(), "eventType");
        required(event.paymentId(), "paymentId");
        required(event.userId(), "userId");
        required(event.walletId(), "walletId");
        required(event.amount(), "amount");
        required(event.occurredAt(), "occurredAt");
        if (!com.smartpayut.transaction.messaging.config.TransactionRabbitConfig.PAYMENT_ROUTING_KEYS
                .contains(event.eventType())) {
            throw new IllegalArgumentException("Evento Payment no soportado: " + event.eventType());
        }
    }

    private String walletCorrelation(WalletEvent event) {
        if (event.referenceId() != null && !event.referenceId().isBlank()) {
            return event.referenceId();
        }
        if (event.movementId() != null) {
            return event.movementId().toString();
        }
        return event.walletId().toString();
    }

    private TransactionType walletType(String eventType) {
        return switch (eventType) {
            case "wallet.created" -> TransactionType.WALLET_CREATED;
            case "wallet.credited" -> TransactionType.CREDIT;
            case "wallet.debited" -> TransactionType.DEBIT;
            case "wallet.refunded" -> TransactionType.REFUND;
            default -> throw new IllegalArgumentException("Evento Wallet no soportado: " + eventType);
        };
    }

    private TransactionType paymentType(String eventType) {
        if (eventType.startsWith("topup.")) {
            return TransactionType.TOP_UP;
        }
        if (eventType.equals("payment.refunded")) {
            return TransactionType.REFUND;
        }
        return TransactionType.PAYMENT;
    }

    private TransactionStatus paymentStatus(String eventType, String status) {
        if (eventType.endsWith(".failed")) {
            return TransactionStatus.FAILED;
        }
        if (eventType.equals("payment.refunded")) {
            return TransactionStatus.REFUNDED;
        }
        if ("FAILED".equalsIgnoreCase(status)) {
            return TransactionStatus.FAILED;
        }
        return TransactionStatus.COMPLETED;
    }

    private <T> T required(T value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " es obligatorio en el evento.");
        }
        return value;
    }

    private String requiredText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " es obligatorio en el evento.");
        }
        return value;
    }
}
