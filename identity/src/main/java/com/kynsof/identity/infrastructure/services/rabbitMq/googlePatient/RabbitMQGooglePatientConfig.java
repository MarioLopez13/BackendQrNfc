package com.kynsof.identity.infrastructure.services.rabbitMq.googlePatient;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de RabbitMQ para el evento de registro de pacientes vía Google OAuth.
 * Este evento es diferente al evento normal de pacientes porque:
 * 1. Se origina en identity (no en patients)
 * 2. El ID del paciente es el mismo que el keycloakId
 * 3. Crea el paciente en múltiples servicios: patients, treatments, cirugia, calendar
 */
@Configuration
public class RabbitMQGooglePatientConfig {

    public static final String GOOGLE_PATIENT_EXCHANGE = "google.patient.topic.exchange";
    public static final String GOOGLE_PATIENT_ROUTING_KEY = "google.patient.routing.key";

    @Bean
    public TopicExchange googlePatientExchange() {
        return new TopicExchange(GOOGLE_PATIENT_EXCHANGE);
    }
}
