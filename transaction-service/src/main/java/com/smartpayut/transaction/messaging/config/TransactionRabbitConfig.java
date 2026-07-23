package com.smartpayut.transaction.messaging.config;

import java.util.List;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TransactionRabbitConfig {

    public static final List<String> WALLET_ROUTING_KEYS = List.of(
            "wallet.created", "wallet.credited", "wallet.debited", "wallet.refunded");

    public static final List<String> PAYMENT_ROUTING_KEYS = List.of(
            "payment.completed", "payment.failed", "payment.refunded",
            "topup.completed", "topup.failed");

    @Bean
    TopicExchange transactionWalletExchange(
            @Value("${transaction.rabbitmq.wallet-exchange}") String exchangeName) {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    DirectExchange transactionPaymentExchange(
            @Value("${transaction.rabbitmq.payment-exchange}") String exchangeName) {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    DirectExchange transactionDeadLetterExchange(
            @Value("${transaction.rabbitmq.dead-letter-exchange}") String exchangeName) {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    Queue transactionEventsQueue(
            @Value("${transaction.rabbitmq.queue}") String queueName,
            @Value("${transaction.rabbitmq.dead-letter-exchange}") String deadLetterExchange,
            @Value("${transaction.rabbitmq.dead-letter-queue}") String deadLetterQueue) {
        return QueueBuilder.durable(queueName)
                .deadLetterExchange(deadLetterExchange)
                .deadLetterRoutingKey(deadLetterQueue)
                .build();
    }

    @Bean
    Queue transactionDeadLetterQueue(
            @Value("${transaction.rabbitmq.dead-letter-queue}") String queueName) {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    Declarables walletEventBindings(
            Queue transactionEventsQueue,
            TopicExchange transactionWalletExchange) {
        List<Binding> bindings = WALLET_ROUTING_KEYS.stream()
                .map(key -> BindingBuilder.bind(transactionEventsQueue).to(transactionWalletExchange).with(key))
                .toList();
        return new Declarables(bindings);
    }

    @Bean
    Declarables paymentEventBindings(
            Queue transactionEventsQueue,
            DirectExchange transactionPaymentExchange) {
        List<Binding> bindings = PAYMENT_ROUTING_KEYS.stream()
                .map(key -> BindingBuilder.bind(transactionEventsQueue).to(transactionPaymentExchange).with(key))
                .toList();
        return new Declarables(bindings);
    }

    @Bean
    Binding transactionDeadLetterBinding(
            Queue transactionDeadLetterQueue,
            DirectExchange transactionDeadLetterExchange,
            @Value("${transaction.rabbitmq.dead-letter-queue}") String routingKey) {
        return BindingBuilder.bind(transactionDeadLetterQueue)
                .to(transactionDeadLetterExchange)
                .with(routingKey);
    }

    @Bean
    SimpleRabbitListenerContainerFactory transactionListenerFactory(
            ConnectionFactory connectionFactory,
            @Value("${spring.rabbitmq.listener.simple.auto-startup:true}") boolean autoStartup) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAutoStartup(autoStartup);
        factory.setDefaultRequeueRejected(false);
        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(500, 2, 2000)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build());
        return factory;
    }
}
