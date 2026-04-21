//package com.kynsof.identity.infrastructure.services.rabbitMq.otp;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.kynsof.identity.infrastructure.services.rabbitMq.RabbitMQConfig;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.stereotype.Service;
//
//@Service
//public class OtpMessageProducer {
//
//    private final RabbitTemplate rabbitTemplate;
//    private final ObjectMapper objectMapper;
//
//    public OtpMessageProducer(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
//        this.rabbitTemplate = rabbitTemplate;
//        this.objectMapper = objectMapper;
//    }
//
//    public void sendOtpMessage(String email, String otpCode, String name) {
//        try {
//            OtpMessageDTO otpMessage = new OtpMessageDTO(email, otpCode, name);
//            String jsonMessage = objectMapper.writeValueAsString(otpMessage);
//            rabbitTemplate.convertAndSend(RabbitMQConfig.OTP_QUEUE, jsonMessage);
//            System.out.println("📤 Mensaje OTP enviado a RabbitMQ: " + jsonMessage);
//        } catch (JsonProcessingException e) {
//            System.err.println("❌ Error al serializar el mensaje OTP: " + e.getMessage());
//        }
//    }
//}