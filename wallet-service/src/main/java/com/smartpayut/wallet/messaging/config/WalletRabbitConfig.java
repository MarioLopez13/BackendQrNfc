package com.smartpayut.wallet.messaging.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

@Configuration
public class WalletRabbitConfig {
    public static final String IDENTITY_EXCHANGE = "smartpayut.identity.events",
            IDENTITY_CREATED = "identity.user.created", IDENTITY_QUEUE = "smartpayut.wallet.identity-user-created",
            WALLET_EXCHANGE = "smartpayut.wallet.events", DLX = "smartpayut.wallet.dlx",
            DLQ = "smartpayut.wallet.identity-user-created.dlq";

    @Bean
    TopicExchange identityExchange() {
        return new TopicExchange(IDENTITY_EXCHANGE, true, false);
    }

    @Bean
    TopicExchange walletExchange() {
        return new TopicExchange(WALLET_EXCHANGE, true, false);
    }

    @Bean
    DirectExchange walletDlx() {
        return new DirectExchange(DLX, true, false);
    }

    @Bean
    Queue identityQueue() {
        return QueueBuilder.durable(IDENTITY_QUEUE).deadLetterExchange(DLX).deadLetterRoutingKey(DLQ).build();
    }

    @Bean
    Queue identityDlq() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    Binding identityBinding() {
        return BindingBuilder.bind(identityQueue()).to(identityExchange()).with(IDENTITY_CREATED);
    }

    @Bean
    Binding dlqBinding() {
        return BindingBuilder.bind(identityDlq()).to(walletDlx()).with(DLQ);
    }

    @Bean
    MessageConverter walletJsonConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    SimpleRabbitListenerContainerFactory walletListenerFactory(ConnectionFactory cf, MessageConverter converter,
            @Value("${spring.rabbitmq.listener.simple.auto-startup:true}") boolean autoStartup) {
        var f = new SimpleRabbitListenerContainerFactory();
        f.setConnectionFactory(cf);
        f.setMessageConverter(converter);
        f.setAutoStartup(autoStartup);
        f.setDefaultRequeueRejected(false);
        f.setAdviceChain(RetryInterceptorBuilder.stateless().maxAttempts(3).backOffOptions(500, 2, 2000)
                .recoverer(new RejectAndDontRequeueRecoverer()).build());
        return f;
    }
}
