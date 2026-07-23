package com.smartpayut.identity.messaging.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.*;
import org.springframework.context.annotation.*;

@Configuration
public class IdentityRabbitConfig {
    public static final String EXCHANGE = "smartpayut.identity.events";
    public static final String USER_CREATED = "identity.user.created";

    @Bean
    TopicExchange identityEventsExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    MessageConverter identityMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
