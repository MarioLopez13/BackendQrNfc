package com.smartpayut.wallet.integration;

import com.smartpayut.wallet.event.WalletEvent;
import com.smartpayut.wallet.messaging.config.WalletRabbitConfig;
import com.smartpayut.wallet.messaging.publisher.WalletEventPublisher;
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
    static RabbitMQContainer mq = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    @Test
    void publicaWalletCreatedEnRabbitReal() {
        var cf = new CachingConnectionFactory(mq.getHost(), mq.getAmqpPort());
        cf.setUsername(mq.getAdminUsername());
        cf.setPassword(mq.getAdminPassword());
        try {
            var admin = new RabbitAdmin(cf);
            var ex = new TopicExchange(WalletRabbitConfig.WALLET_EXCHANGE, true, false);
            var q = new AnonymousQueue();
            admin.declareExchange(ex);
            admin.declareQueue(q);
            admin.declareBinding(BindingBuilder.bind(q).to(ex).with("wallet.created"));
            var t = new RabbitTemplate(cf);
            t.setMessageConverter(new Jackson2JsonMessageConverter());
            new WalletEventPublisher(t).publish(
                    new WalletEvent(UUID.randomUUID(), "wallet.created", 1, OffsetDateTime.now(), UUID.randomUUID(),
                            UUID.randomUUID(), null, null, null, java.math.BigDecimal.ZERO, "USD", null, null));
            assertNotNull(t.receive(q.getName(), 5000));
        } finally {
            cf.destroy();
        }
    }
}
