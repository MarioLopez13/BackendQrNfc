package com.kynsof.identity.infrastructure.services.rabbitMq.eventPublisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kynsof.identity.infrastructure.services.rabbitMq.RabbitMQBusinessConfig;
import com.kynsof.identity.infrastructure.services.rabbitMq.dto.BusinessRabbitMQDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EventBusinessPublisherService {

    private final RabbitTemplate rabbitTemplate;
    private final TopicExchange businessExchange;
    private final ObjectMapper objectMapper;

    @Autowired
    public EventBusinessPublisherService(RabbitTemplate rabbitTemplate,
                                 @Qualifier("businessExchange") TopicExchange businessExchange,
                                 ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.businessExchange = businessExchange;
        this.objectMapper = objectMapper;
    }

    public void publishBusinessEvent(BusinessRabbitMQDto event) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(
                    businessExchange.getName(),
                    RabbitMQBusinessConfig.BUSINESS_CREATED_ROUTING_KEY,
                    jsonMessage
            );
            log.info("Business event published to RabbitMQ: {}", jsonMessage);
        } catch (JsonProcessingException e) {
            log.error("Error serializing business event: {}", e.getMessage());
        }
    }
}
