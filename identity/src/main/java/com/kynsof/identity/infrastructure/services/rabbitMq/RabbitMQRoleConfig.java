package com.kynsof.identity.infrastructure.services.rabbitMq;


import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQRoleConfig {
    public static final String MANAGE_ROLE_EXCHANGE = "manage.role.topic.exchange";
    public static final String MANAGE_ROLE_CREATED_ROUTING_KEY = "manage.role.routing.key";

    @Bean
    public TopicExchange manageRoleExchange() {
        return new TopicExchange(MANAGE_ROLE_EXCHANGE);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
