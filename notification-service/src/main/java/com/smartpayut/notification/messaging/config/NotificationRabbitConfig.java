package com.smartpayut.notification.messaging.config;

import java.util.List;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationRabbitConfig {

    public static final String IDENTITY_USER_CREATED = "identity.user.created";
    public static final List<String> WALLET_KEYS = List.of(
            "wallet.created", "wallet.credited", "wallet.debited", "wallet.refunded");
    public static final List<String> PAYMENT_KEYS = List.of(
            "payment.completed", "payment.failed", "payment.refunded",
            "topup.completed", "topup.failed");

    @Bean
    TopicExchange notificationIdentityExchange(@Value("${notification.rabbitmq.identity-exchange}") String name) {
        return new TopicExchange(name, true, false);
    }

    @Bean
    TopicExchange notificationWalletExchange(@Value("${notification.rabbitmq.wallet-exchange}") String name) {
        return new TopicExchange(name, true, false);
    }

    @Bean
    DirectExchange notificationPaymentExchange(@Value("${notification.rabbitmq.payment-exchange}") String name) {
        return new DirectExchange(name, true, false);
    }

    @Bean
    DirectExchange notificationDlx(@Value("${notification.rabbitmq.dead-letter-exchange}") String name) {
        return new DirectExchange(name, true, false);
    }

    @Bean
    Queue notificationQueue(
            @Value("${notification.rabbitmq.queue}") String name,
            @Value("${notification.rabbitmq.dead-letter-exchange}") String dlx,
            @Value("${notification.rabbitmq.dead-letter-queue}") String dlq) {
        return QueueBuilder.durable(name).deadLetterExchange(dlx).deadLetterRoutingKey(dlq).build();
    }

    @Bean
    Queue notificationDlq(@Value("${notification.rabbitmq.dead-letter-queue}") String name) {
        return QueueBuilder.durable(name).build();
    }

    @Bean
    Binding notificationIdentityBinding(
            Queue notificationQueue,
            TopicExchange notificationIdentityExchange) {
        return BindingBuilder.bind(notificationQueue)
                .to(notificationIdentityExchange)
                .with(IDENTITY_USER_CREATED);
    }

    @Bean
    Declarables notificationWalletBindings(Queue notificationQueue, TopicExchange notificationWalletExchange) {
        List<Binding> bindings = WALLET_KEYS.stream()
                .map(key -> BindingBuilder.bind(notificationQueue).to(notificationWalletExchange).with(key))
                .toList();
        return new Declarables(bindings);
    }

    @Bean
    Declarables notificationPaymentBindings(Queue notificationQueue, DirectExchange notificationPaymentExchange) {
        List<Binding> bindings = PAYMENT_KEYS.stream()
                .map(key -> BindingBuilder.bind(notificationQueue).to(notificationPaymentExchange).with(key))
                .toList();
        return new Declarables(bindings);
    }

    @Bean
    Binding notificationDlqBinding(
            Queue notificationDlq,
            DirectExchange notificationDlx,
            @Value("${notification.rabbitmq.dead-letter-queue}") String routingKey) {
        return BindingBuilder.bind(notificationDlq).to(notificationDlx).with(routingKey);
    }

    @Bean
    SimpleRabbitListenerContainerFactory notificationListenerFactory(
            ConnectionFactory connectionFactory,
            @Value("${spring.rabbitmq.listener.simple.auto-startup:true}") boolean autoStartup) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAutoStartup(autoStartup);
        factory.setDefaultRequeueRejected(false);
        factory.setAdviceChain(RetryInterceptorBuilder.stateless().maxAttempts(3)
                .backOffOptions(500, 2, 2000)
                .recoverer(new RejectAndDontRequeueRecoverer()).build());
        return factory;
    }
}
