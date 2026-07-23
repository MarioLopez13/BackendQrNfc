package com.smartpayut.payment.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.smartpayut.payment.domain.entity.Payment;
import com.smartpayut.payment.domain.enumeration.PaymentMethod;
import com.smartpayut.payment.repository.PaymentRepository;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class PaymentRepositoryIntegrationTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    void storesAndFindsPaymentByIdempotencyKey() {
        Payment payment = new Payment(
                UUID.randomUUID(),
                UUID.randomUUID(),
                PaymentMethod.QR,
                new BigDecimal("0.35"),
                "integration-qr-001",
                "BUS-100",
                "Ruta Universitaria");

        paymentRepository.saveAndFlush(payment);

        assertThat(paymentRepository.findByIdempotencyKey("integration-qr-001"))
                .get()
                .extracting(Payment::getId)
                .isEqualTo(payment.getId());
    }
}
