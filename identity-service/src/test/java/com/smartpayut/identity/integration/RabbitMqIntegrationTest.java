package com.smartpayut.identity.integration;

import com.smartpayut.identity.event.IdentityUserCreatedEvent;
import com.smartpayut.identity.messaging.config.IdentityRabbitConfig;
import com.smartpayut.identity.messaging.publisher.IdentityEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers(disabledWithoutDocker = true)
class RabbitMqIntegrationTest {
    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    @Test
    void publicaEventoEnRabbitReal() {
        var cf = new CachingConnectionFactory(rabbitmq.getHost(), rabbitmq.getAmqpPort());
        cf.setUsername(rabbitmq.getAdminUsername());
        cf.setPassword(rabbitmq.getAdminPassword());
        try {
            var admin = new RabbitAdmin(cf);
            var exchange = new TopicExchange(IdentityRabbitConfig.EXCHANGE, true, false);
            var queue = new AnonymousQueue();
            admin.declareExchange(exchange);
            admin.declareQueue(queue);
            admin.declareBinding(BindingBuilder.bind(queue).to(exchange).with(IdentityRabbitConfig.USER_CREATED));
            var template = new RabbitTemplate(cf);
            template.setMessageConverter(new Jackson2JsonMessageConverter());
            new IdentityEventPublisher(template).publish(
                    new IdentityUserCreatedEvent(UUID.randomUUID(), "identity.user.created", 1, OffsetDateTime.now(),
                            UUID.randomUUID(), UUID.randomUUID(), "u", "e@x.com", "n", "l", "ACTIVE"));
            assertNotNull(template.receive(queue.getName(), 5000));
        } finally {
            cf.destroy();
        }
    }
}
