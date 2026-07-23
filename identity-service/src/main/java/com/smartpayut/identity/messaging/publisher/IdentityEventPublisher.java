package com.smartpayut.identity.messaging.publisher;

import com.smartpayut.identity.event.IdentityUserCreatedEvent;
import com.smartpayut.identity.exception.IdentityException;
import com.smartpayut.identity.messaging.config.IdentityRabbitConfig;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class IdentityEventPublisher {
    private final RabbitTemplate rabbit;

    public IdentityEventPublisher(RabbitTemplate rabbit) {
        this.rabbit = rabbit;
    }

    public void publish(IdentityUserCreatedEvent event) {
        try {
            rabbit.convertAndSend(IdentityRabbitConfig.EXCHANGE, IdentityRabbitConfig.USER_CREATED, event);
        } catch (AmqpException e) {
            throw new IdentityException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Usuario no creado: RabbitMQ no está disponible");
        }
    }
}
