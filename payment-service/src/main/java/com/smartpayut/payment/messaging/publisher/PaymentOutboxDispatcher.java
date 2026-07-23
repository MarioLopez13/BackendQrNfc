package com.smartpayut.payment.messaging.publisher;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.smartpayut.payment.domain.entity.PaymentOutboxEvent;
import com.smartpayut.payment.domain.enumeration.OutboxStatus;
import com.smartpayut.payment.repository.PaymentOutboxEventRepository;

@Component
@ConditionalOnProperty(
        name = "payment.outbox.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class PaymentOutboxDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentOutboxDispatcher.class);

    private final PaymentOutboxEventRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final DirectExchange paymentEventsExchange;
    private final int batchSize;
    private final long confirmTimeoutSeconds;

    public PaymentOutboxDispatcher(
            PaymentOutboxEventRepository outboxRepository,
            RabbitTemplate rabbitTemplate,
            DirectExchange paymentEventsExchange,
            @Value("${payment.outbox.batch-size:20}") int batchSize,
            @Value("${payment.outbox.confirm-timeout-seconds:5}") long confirmTimeoutSeconds) {
        this.outboxRepository = outboxRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.paymentEventsExchange = paymentEventsExchange;
        this.batchSize = batchSize;
        this.confirmTimeoutSeconds = confirmTimeoutSeconds;
    }

    @Scheduled(fixedDelayString = "${payment.outbox.fixed-delay-ms:1000}")
    public void dispatchPending() {
        List<PaymentOutboxEvent> events = outboxRepository
                .findByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
                        OutboxStatus.PENDING,
                        OffsetDateTime.now(ZoneOffset.UTC),
                        PageRequest.of(0, batchSize));
        events.forEach(this::dispatch);
    }

    void dispatch(PaymentOutboxEvent event) {
        try {
            CorrelationData correlation = new CorrelationData(event.getEventId());
            Message message = MessageBuilder
                    .withBody(event.getPayload().getBytes(StandardCharsets.UTF_8))
                    .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                    .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                    .setMessageId(event.getEventId())
                    .build();

            rabbitTemplate.send(
                    paymentEventsExchange.getName(),
                    event.getEventType(),
                    message,
                    correlation);

            CorrelationData.Confirm confirm = correlation.getFuture()
                    .get(confirmTimeoutSeconds, TimeUnit.SECONDS);
            if (!confirm.isAck()) {
                throw new IllegalStateException("RabbitMQ rechazó el evento: " + confirm.getReason());
            }
            if (correlation.getReturned() != null) {
                throw new IllegalStateException("RabbitMQ devolvió el evento sin enrutar.");
            }

            event.markPublished();
            outboxRepository.save(event);
        } catch (Exception exception) {
            int delaySeconds = Math.min(60, 1 << Math.min(event.getAttempts(), 6));
            event.scheduleRetry(exception.getMessage(), delaySeconds);
            outboxRepository.save(event);
            LOGGER.warn(
                    "Evento {} pendiente de reintento después de fallo de publicación.",
                    event.getEventId());
        }
    }
}
