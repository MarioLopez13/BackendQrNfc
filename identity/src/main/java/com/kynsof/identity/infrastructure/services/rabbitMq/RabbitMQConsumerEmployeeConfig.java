package com.kynsof.identity.infrastructure.services.rabbitMq;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConsumerEmployeeConfig {
    //Este valor debe de ser igual al micro que produce
    public static final String EMPLOYEE_EXCHANGE = "employee.exchange";

    //Este valor debe de ser igual al micro que produce
    public static final String EMPLOYEE_CREATED_ROUTING_KEY = "employee.routing.key";

    //Este valor es unico para el micro.
    public static final String EMPLOYEE_QUEUE = "employee.queue.identity";

    @Bean
    public TopicExchange employeeExchange() {
        return new TopicExchange(EMPLOYEE_EXCHANGE);
    }

    // Cada microservicio debe usar un nombre de cola ÚNICO
    @Bean
    public Queue employeeCreatedQueue() {
        return new Queue(EMPLOYEE_QUEUE, true);
    }

    @Bean
    public Binding employeeCreatedBinding() {
        return BindingBuilder.bind(employeeCreatedQueue())
                .to(employeeExchange())
                .with(EMPLOYEE_CREATED_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}