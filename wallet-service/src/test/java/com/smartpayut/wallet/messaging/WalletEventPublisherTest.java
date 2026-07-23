package com.smartpayut.wallet.messaging;

import com.smartpayut.wallet.event.WalletEvent;
import com.smartpayut.wallet.messaging.config.WalletRabbitConfig;
import com.smartpayut.wallet.messaging.publisher.WalletEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import java.time.OffsetDateTime;
import java.util.UUID;
import static org.mockito.Mockito.*;

class WalletEventPublisherTest {
    @Test
    void publicaRoutingKeyDelEvento() {
        RabbitTemplate r = mock(RabbitTemplate.class);
        var e = new WalletEvent(UUID.randomUUID(), "wallet.debited", 1, OffsetDateTime.now(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), null, null, null, "USD", null, "k");
        new WalletEventPublisher(r).publish(e);
        verify(r).convertAndSend(WalletRabbitConfig.WALLET_EXCHANGE, "wallet.debited", e);
    }
}
