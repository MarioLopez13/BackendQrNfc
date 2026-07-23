package com.smartpayut.payment.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartpayut.payment.domain.entity.Payment;
import com.smartpayut.payment.domain.enumeration.OutboxStatus;
import com.smartpayut.payment.domain.enumeration.PaymentMethod;
import com.smartpayut.payment.domain.enumeration.PaymentStatus;
import com.smartpayut.payment.messaging.publisher.PaymentEventPublisher;
import com.smartpayut.payment.repository.PaymentOutboxEventRepository;
import com.smartpayut.payment.repository.PaymentRepository;
import com.smartpayut.payment.service.PaymentEventStateService;

@DataJpaTest
@ActiveProfiles("test")
@Import({
        PaymentEventPublisher.class,
        PaymentEventStateService.class,
        PaymentOutboxIntegrationTest.ObjectMapperConfiguration.class
})
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class PaymentOutboxIntegrationTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentOutboxEventRepository outboxRepository;

    @Autowired
    private PaymentEventStateService eventStateService;

    @Test
    void storesCompletedPaymentAndPendingEventAtomically() {
        Payment payment = paymentRepository.save(payment("qr-atomic", PaymentMethod.QR));

        eventStateService.complete(
                payment,
                new BigDecimal("10.00"),
                new BigDecimal("9.65"),
                "payment.completed");

        Payment persisted = paymentRepository.findById(payment.getId()).orElseThrow();
        var outbox = outboxRepository
                .findByPaymentIdAndEventType(payment.getId(), "payment.completed")
                .orElseThrow();

        assertThat(persisted.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(outbox.getPayload()).contains(payment.getId().toString());
    }

    @Test
    void deterministicEventDoesNotCreateSecondOutboxEntry() {
        Payment payment = paymentRepository.save(payment("nfc-idempotent", PaymentMethod.NFC));

        eventStateService.complete(
                payment,
                new BigDecimal("5.00"),
                new BigDecimal("4.65"),
                "payment.completed");
        eventStateService.complete(
                payment,
                new BigDecimal("5.00"),
                new BigDecimal("4.65"),
                "payment.completed");

        assertThat(outboxRepository.count()).isOne();
    }

    private Payment payment(String key, PaymentMethod method) {
        return new Payment(
                UUID.randomUUID(),
                UUID.randomUUID(),
                method,
                new BigDecimal("0.35"),
                key,
                "BUS-TEST",
                "Ruta Test");
    }

    @TestConfiguration
    static class ObjectMapperConfiguration {

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper().findAndRegisterModules();
        }
    }
}
