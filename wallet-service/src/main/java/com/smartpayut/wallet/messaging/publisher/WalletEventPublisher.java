package com.smartpayut.wallet.messaging.publisher;

import com.smartpayut.wallet.event.WalletEvent;
import com.smartpayut.wallet.exception.WalletException;
import com.smartpayut.wallet.messaging.config.WalletRabbitConfig;
import org.springframework.amqp.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class WalletEventPublisher {
    private final RabbitTemplate rabbit;

    public WalletEventPublisher(RabbitTemplate r) {
        rabbit = r;
    }

    public void publish(WalletEvent e) {
        try {
            rabbit.convertAndSend(WalletRabbitConfig.WALLET_EXCHANGE, e.eventType(), e);
        } catch (AmqpException x) {
            throw new WalletException(HttpStatus.SERVICE_UNAVAILABLE, "RabbitMQ no está disponible");
        }
    }
}
