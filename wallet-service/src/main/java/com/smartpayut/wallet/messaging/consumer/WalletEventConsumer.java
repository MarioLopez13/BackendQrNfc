package com.smartpayut.wallet.messaging.consumer;

import com.smartpayut.wallet.event.IdentityUserCreatedEvent;
import com.smartpayut.wallet.messaging.config.WalletRabbitConfig;
import com.smartpayut.wallet.service.WalletService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class WalletEventConsumer {
    private final WalletService service;

    public WalletEventConsumer(WalletService s) {
        service = s;
    }

    @RabbitListener(queues = WalletRabbitConfig.IDENTITY_QUEUE, containerFactory = "walletListenerFactory")
    public void identityUserCreated(IdentityUserCreatedEvent event) {
        service.createFrom(event);
    }
}
