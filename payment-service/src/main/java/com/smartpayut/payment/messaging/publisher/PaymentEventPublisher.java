package com.smartpayut.payment.messaging.publisher;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.smartpayut.payment.domain.entity.Payment;
import com.smartpayut.payment.event.PaymentEvent;

@Component
public class PaymentEventPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final DirectExchange paymentEventsExchange;

    public PaymentEventPublisher(RabbitTemplate rabbitTemplate, DirectExchange paymentEventsExchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.paymentEventsExchange = paymentEventsExchange;
    }

    public void publish(String routingKey, Payment payment) {
        PaymentEvent event = new PaymentEvent(
                UUID.randomUUID().toString(),
                routingKey,
                1,
                OffsetDateTime.now(ZoneOffset.UTC),
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
            rabbitTemplate.convertAndSend(paymentEventsExchange.getName(), routingKey, event);
        } catch (RuntimeException exception) {
            LOGGER.error("No fue posible publicar el evento {} del pago {}.", routingKey, payment.getId(), exception);
        }
    }
}
