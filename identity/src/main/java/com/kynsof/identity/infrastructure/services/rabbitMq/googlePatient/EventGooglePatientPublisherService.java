package com.kynsof.identity.infrastructure.services.rabbitMq.googlePatient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Servicio para publicar eventos de registro de paciente vía Google OAuth.
 * Publica al exchange google.patient.topic.exchange que será consumido por:
 * - patients
 * - treatments
 * - cirugia
 * - calendar
 */
@Slf4j
@Service
public class EventGooglePatientPublisherService {

    private final RabbitTemplate rabbitTemplate;
    private final TopicExchange googlePatientExchange;
    private final ObjectMapper objectMapper;

    @Autowired
    public EventGooglePatientPublisherService(
            RabbitTemplate rabbitTemplate,
            @Qualifier("googlePatientExchange") TopicExchange googlePatientExchange,
            ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.googlePatientExchange = googlePatientExchange;
        this.objectMapper = objectMapper;
    }

    /**
     * Publica el evento de registro de paciente Google a RabbitMQ.
     * El evento será consumido por patients, treatments, cirugia y calendar.
     *
     * @param event DTO con los datos del paciente registrado
     */
    public void publishGooglePatientRegisteredEvent(GooglePatientRegisteredDto event) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(
                    googlePatientExchange.getName(),
                    RabbitMQGooglePatientConfig.GOOGLE_PATIENT_ROUTING_KEY,
                    jsonMessage
            );
            log.info("[GooglePatient] Evento publicado a RabbitMQ: id={}, email={}",
                    event.getId(), event.getEmail());
        } catch (JsonProcessingException e) {
            log.error("[GooglePatient] Error al serializar evento: {}", e.getMessage());
            throw new RuntimeException("Error al publicar evento de paciente Google", e);
        }
    }
}
