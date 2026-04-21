package com.kynsof.identity.infrastructure.services.rabbitMq.welcome;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kynsof.identity.infrastructure.services.rabbitMq.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class WelcomeMessageProducer {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper; // JSON Mapper

    public WelcomeMessageProducer(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendWelcomeMessage(String email, String firstname, String lastname, String password) {
        try {
            // Crear el objeto DTO
            WelcomeMessageDTO message = new WelcomeMessageDTO(email, firstname, lastname, password);

            // Convertir a JSON String
            String jsonMessage = objectMapper.writeValueAsString(message);

            // Enviar el JSON como String a RabbitMQ
            rabbitTemplate.convertAndSend(RabbitMQConfig.WELCOME_QUEUE, jsonMessage);
            System.out.println("📤 Mensaje enviado a RabbitMQ: " + jsonMessage);

        } catch (JsonProcessingException e) {
            System.err.println("❌ Error al serializar el mensaje: " + e.getMessage());
        }
    }
}