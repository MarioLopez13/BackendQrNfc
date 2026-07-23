package com.smartpayut.transaction.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.smartpayut.transaction.event.PaymentEvent;
import com.smartpayut.transaction.repository.ProcessedEventRepository;
import com.smartpayut.transaction.repository.TransactionRecordRepository;
import com.smartpayut.transaction.service.EventProjectionService;

@DataJpaTest
@ActiveProfiles("test")
@Import(EventProjectionService.class)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class TransactionProjectionIntegrationTest {

    @Autowired
    private EventProjectionService projectionService;

    @Autowired
    private TransactionRecordRepository transactionRepository;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @Test
    void storesProjectionAndProcessedEventAtomically() {
        UUID paymentId = UUID.randomUUID();
        PaymentEvent event = new PaymentEvent(
                "payment-event-001",
                "payment.completed",
                1,
                OffsetDateTime.now(),
                paymentId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "NFC",
                "COMPLETED",
                new BigDecimal("0.35"),
                "USD",
                "BUS-20",
                "Ruta Universitaria",
                null);

        projectionService.process(event);
        projectionService.process(event);

        assertThat(transactionRepository.findByCorrelationId(paymentId.toString())).isPresent();
        assertThat(processedEventRepository.existsById("payment-event-001")).isTrue();
        assertThat(transactionRepository.count()).isOne();
    }
}
