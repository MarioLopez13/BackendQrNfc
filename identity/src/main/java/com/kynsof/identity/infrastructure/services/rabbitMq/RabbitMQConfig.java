package com.kynsof.identity.infrastructure.services.rabbitMq;


import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String WELCOME_QUEUE = "welcome.queue";//Welcome user

    @Bean
    public Queue welcomeQueue() {
        return new Queue(WELCOME_QUEUE, true);
    }

    public static final String OTP_QUEUE = "otpQueue"; // Nueva cola para OTP

    @Bean
    public Queue otpQueue() {
        return new Queue(OTP_QUEUE, true);
    }
}