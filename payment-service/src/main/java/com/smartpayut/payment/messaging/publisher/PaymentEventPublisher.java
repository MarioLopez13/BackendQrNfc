package com.smartpayut.payment.messaging.publisher;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartpayut.payment.domain.entity.Payment;
import com.smartpayut.payment.domain.entity.PaymentOutboxEvent;
import com.smartpayut.payment.event.PaymentEvent;
import com.smartpayut.payment.repository.PaymentOutboxEventRepository;

@Component
public class PaymentEventPublisher {

    private final PaymentOutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public PaymentEventPublisher(
            PaymentOutboxEventRepository outboxRepository,
            ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    public void publish(String routingKey, Payment payment) {
        if (outboxRepository.findByPaymentIdAndEventType(payment.getId(), routingKey).isPresent()) {
            return;
        }

        String eventId = deterministicEventId(payment.getId(), routingKey);
        PaymentEvent event = new PaymentEvent(
                eventId,
                routingKey,
                1,
                occurredAt(payment),
                payment.getId(),
                payment.getUserAccountId(),
                payment.getWalletId(),
                payment.getMethod().name(),
                payment.getStatus().name(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getBusCode(),
                payment.getRouteName(),
                payment.getFailureReason());

        try {
            outboxRepository.save(new PaymentOutboxEvent(
                    eventId,
                    payment.getId(),
                    routingKey,
                    objectMapper.writeValueAsString(event)));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("No fue posible serializar el evento del pago.", exception);
        }
    }

    private String deterministicEventId(UUID paymentId, String routingKey) {
        String source = paymentId + ":" + routingKey;
        return UUID.nameUUIDFromBytes(source.getBytes(StandardCharsets.UTF_8)).toString();
    }

    private OffsetDateTime occurredAt(Payment payment) {
        if (payment.getRefundedAt() != null) {
            return payment.getRefundedAt();
        }
        if (payment.getCompletedAt() != null) {
            return payment.getCompletedAt();
        }
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
