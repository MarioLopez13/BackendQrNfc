package com.kynsof.identity.infrastructure.services.rabbitMq.balance;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de RabbitMQ para consumir eventos de actualización de balance desde payment.
 */
@Configuration
public class RabbitMQBalanceConsumerConfig {

    // Debe coincidir con el productor (payment)
    public static final String BALANCE_EXCHANGE = "balance.topic.exchange";
    public static final String BALANCE_UPDATED_ROUTING_KEY = "balance.updated.routing.key";

    // Cola única para identity
    public static final String BALANCE_QUEUE = "balance.queue.identity";

    @Bean(name = "balanceExchange")
    public TopicExchange balanceExchange() {
        return new TopicExchange(BALANCE_EXCHANGE);
    }

    @Bean(name = "balanceUpdatedQueue")
    public Queue balanceUpdatedQueue() {
        return new Queue(BALANCE_QUEUE, true);
    }

    @Bean
    public Binding balanceUpdatedBinding(
            @Qualifier("balanceUpdatedQueue") Queue balanceUpdatedQueue,
            @Qualifier("balanceExchange") TopicExchange balanceExchange) {
        return BindingBuilder
                .bind(balanceUpdatedQueue)
                .to(balanceExchange)
                .with(BALANCE_UPDATED_ROUTING_KEY);
    }

    @Bean(name = "balanceMessageConverter")
    public Jackson2JsonMessageConverter balanceMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean(name = "balanceListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory balanceListenerContainerFactory(
            ConnectionFactory connectionFactory,
            @Qualifier("balanceMessageConverter") Jackson2JsonMessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }
}
