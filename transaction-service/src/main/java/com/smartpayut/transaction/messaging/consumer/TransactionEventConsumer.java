package com.smartpayut.transaction.messaging.consumer;

import java.io.IOException;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartpayut.transaction.event.PaymentEvent;
import com.smartpayut.transaction.event.WalletEvent;
import com.smartpayut.transaction.messaging.config.TransactionRabbitConfig;
import com.smartpayut.transaction.service.EventProjectionService;

@Component
public class TransactionEventConsumer {

    private final ObjectMapper objectMapper;
    private final EventProjectionService projectionService;

    public TransactionEventConsumer(ObjectMapper objectMapper, EventProjectionService projectionService) {
        this.objectMapper = objectMapper;
        this.projectionService = projectionService;
    }

    @RabbitListener(
            queues = "${transaction.rabbitmq.queue}",
            containerFactory = "transactionListenerFactory")
    public void consume(Message message, @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey)
            throws IOException {
        if (TransactionRabbitConfig.WALLET_ROUTING_KEYS.contains(routingKey)) {
            projectionService.process(objectMapper.readValue(message.getBody(), WalletEvent.class));
            return;
        }
        if (TransactionRabbitConfig.PAYMENT_ROUTING_KEYS.contains(routingKey)) {
            projectionService.process(objectMapper.readValue(message.getBody(), PaymentEvent.class));
            return;
        }
        throw new IllegalArgumentException("Routing key no soportada: " + routingKey);
    }
}
