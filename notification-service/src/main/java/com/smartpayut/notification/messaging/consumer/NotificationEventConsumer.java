package com.smartpayut.notification.messaging.consumer;

import java.io.IOException;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartpayut.notification.event.IdentityUserCreatedEvent;
import com.smartpayut.notification.event.PaymentEvent;
import com.smartpayut.notification.event.WalletEvent;
import com.smartpayut.notification.messaging.config.NotificationRabbitConfig;
import com.smartpayut.notification.service.NotificationProjectionService;

@Component
public class NotificationEventConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationProjectionService projectionService;

    public NotificationEventConsumer(ObjectMapper objectMapper, NotificationProjectionService projectionService) {
        this.objectMapper = objectMapper;
        this.projectionService = projectionService;
    }

    @RabbitListener(queues = "${notification.rabbitmq.queue}", containerFactory = "notificationListenerFactory")
    public void consume(Message message, @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey)
            throws IOException {
        if (NotificationRabbitConfig.IDENTITY_USER_CREATED.equals(routingKey)) {
            projectionService.process(objectMapper.readValue(message.getBody(), IdentityUserCreatedEvent.class));
            return;
        }
        if (NotificationRabbitConfig.WALLET_KEYS.contains(routingKey)) {
            projectionService.process(objectMapper.readValue(message.getBody(), WalletEvent.class));
            return;
        }
        if (NotificationRabbitConfig.PAYMENT_KEYS.contains(routingKey)) {
            projectionService.process(objectMapper.readValue(message.getBody(), PaymentEvent.class));
            return;
        }
        throw new IllegalArgumentException("Routing key no soportada: " + routingKey);
    }
}
