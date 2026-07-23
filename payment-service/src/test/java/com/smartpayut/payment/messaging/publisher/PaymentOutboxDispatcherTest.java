package com.smartpayut.payment.messaging.publisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartpayut.payment.domain.entity.Payment;
import com.smartpayut.payment.domain.entity.PaymentOutboxEvent;
import com.smartpayut.payment.domain.enumeration.OutboxStatus;
import com.smartpayut.payment.domain.enumeration.PaymentMethod;
import com.smartpayut.payment.repository.PaymentOutboxEventRepository;

class PaymentOutboxDispatcherTest {

    @Test
    void temporaryRabbitFailureKeepsEventPendingForRetry() {
        PaymentOutboxEventRepository repository = mock(PaymentOutboxEventRepository.class);
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        PaymentOutboxEvent event = event();
        doThrow(new AmqpConnectException(new IllegalStateException("Rabbit unavailable")))
                .when(rabbitTemplate)
                .send(anyString(), anyString(), any(), any(CorrelationData.class));
        PaymentOutboxDispatcher dispatcher = dispatcher(repository, rabbitTemplate);

        dispatcher.dispatch(event);

        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(event.getAttempts()).isEqualTo(1);
        assertThat(event.getLastError()).isNotBlank();
    }

    @Test
    void positivePublisherConfirmMarksEventAsPublished() {
        PaymentOutboxEventRepository repository = mock(PaymentOutboxEventRepository.class);
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        PaymentOutboxEvent event = event();
        doAnswer(invocation -> {
            CorrelationData correlation = invocation.getArgument(3);
            correlation.getFuture().complete(new CorrelationData.Confirm(true, null));
            return null;
        }).when(rabbitTemplate).send(anyString(), anyString(), any(), any(CorrelationData.class));
        PaymentOutboxDispatcher dispatcher = dispatcher(repository, rabbitTemplate);

        dispatcher.dispatch(event);

        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
        assertThat(event.getPublishedAt()).isNotNull();
    }

    @Test
    void pendingEventIsPublishedWhenRabbitRecovers() {
        PaymentOutboxEventRepository repository = mock(PaymentOutboxEventRepository.class);
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        PaymentOutboxEvent event = event();
        doThrow(new AmqpConnectException(new IllegalStateException("Rabbit unavailable")))
                .doAnswer(invocation -> {
                    CorrelationData correlation = invocation.getArgument(3);
                    correlation.getFuture().complete(new CorrelationData.Confirm(true, null));
                    return null;
                })
                .when(rabbitTemplate)
                .send(anyString(), anyString(), any(), any(CorrelationData.class));
        PaymentOutboxDispatcher dispatcher = dispatcher(repository, rabbitTemplate);

        dispatcher.dispatch(event);
        dispatcher.dispatch(event);

        assertThat(event.getAttempts()).isEqualTo(1);
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
        assertThat(event.getPublishedAt()).isNotNull();
    }

    @Test
    void existingDeterministicEventIsNotEnqueuedTwice() {
        PaymentOutboxEventRepository repository = mock(PaymentOutboxEventRepository.class);
        Payment payment = new Payment(
                UUID.randomUUID(),
                UUID.randomUUID(),
                PaymentMethod.QR,
                java.math.BigDecimal.ONE,
                "duplicate-key",
                "BUS",
                "Ruta");
        when(repository.findByPaymentIdAndEventType(payment.getId(), "payment.completed"))
                .thenReturn(Optional.of(event()));
        PaymentEventPublisher publisher = new PaymentEventPublisher(
                repository,
                new ObjectMapper().findAndRegisterModules());

        publisher.publish("payment.completed", payment);

        org.mockito.Mockito.verify(repository, org.mockito.Mockito.never()).save(any());
    }

    private PaymentOutboxDispatcher dispatcher(
            PaymentOutboxEventRepository repository,
            RabbitTemplate rabbitTemplate) {
        return new PaymentOutboxDispatcher(
                repository,
                rabbitTemplate,
                new DirectExchange("smartpayut.payment.events"),
                20,
                1);
    }

    private PaymentOutboxEvent event() {
        return new PaymentOutboxEvent(
                UUID.randomUUID().toString(),
                UUID.randomUUID(),
                "payment.completed",
                "{\"eventType\":\"payment.completed\"}");
    }
}
