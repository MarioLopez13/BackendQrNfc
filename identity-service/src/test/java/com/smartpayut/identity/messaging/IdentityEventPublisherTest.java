package com.smartpayut.identity.messaging;

import com.smartpayut.identity.event.IdentityUserCreatedEvent;
import com.smartpayut.identity.messaging.config.IdentityRabbitConfig;
import com.smartpayut.identity.messaging.publisher.IdentityEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import java.time.OffsetDateTime;
import java.util.UUID;
import static org.mockito.Mockito.*;

class IdentityEventPublisherTest {
    @Test
    void publicaConExchangeYRoutingKey() {
        RabbitTemplate rabbit = mock(RabbitTemplate.class);
        var event = new IdentityUserCreatedEvent(UUID.randomUUID(), "identity.user.created", 1, OffsetDateTime.now(),
                UUID.randomUUID(), UUID.randomUUID(), "u", "e@x.com", "n", "l", "ACTIVE");
        new IdentityEventPublisher(rabbit).publish(event);
        verify(rabbit).convertAndSend(IdentityRabbitConfig.EXCHANGE, IdentityRabbitConfig.USER_CREATED, event);
    }
}
