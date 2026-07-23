package com.smartpayut.transaction.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.smartpayut.transaction.domain.entity.ProcessedEvent;
import com.smartpayut.transaction.domain.entity.TransactionRecord;
import com.smartpayut.transaction.event.PaymentEvent;
import com.smartpayut.transaction.event.WalletEvent;
import com.smartpayut.transaction.repository.ProcessedEventRepository;
import com.smartpayut.transaction.repository.TransactionRecordRepository;

@ExtendWith(MockitoExtension.class)
class EventProjectionServiceTest {

    @Mock
    private TransactionRecordRepository transactionRepository;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    private EventProjectionService service;

    @BeforeEach
    void setUp() {
        service = new EventProjectionService(transactionRepository, processedEventRepository);
    }

    @Test
    void projectsWalletDebit() {
        UUID eventId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        WalletEvent event = new WalletEvent(
                eventId,
                "wallet.debited",
                1,
                OffsetDateTime.now(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("0.35"),
                new BigDecimal("5.00"),
                new BigDecimal("4.65"),
                "USD",
                paymentId.toString(),
                "wallet-operation-1");
        when(processedEventRepository.existsById(eventId.toString())).thenReturn(false);
        when(transactionRepository.findByCorrelationId(paymentId.toString())).thenReturn(Optional.empty());

        service.process(event);

        verify(transactionRepository).save(any(TransactionRecord.class));
        verify(processedEventRepository).save(any(ProcessedEvent.class));
    }

    @Test
    void ignoresAlreadyProcessedPaymentEvent() {
        PaymentEvent event = new PaymentEvent(
                "event-001",
                "payment.completed",
                1,
                OffsetDateTime.now(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "QR",
                "COMPLETED",
                new BigDecimal("0.35"),
                "USD",
                "BUS-10",
                "Ruta Central",
                null);
        when(processedEventRepository.existsById("event-001")).thenReturn(true);

        service.process(event);

        verify(transactionRepository, never()).save(any());
        verify(processedEventRepository, never()).save(any());
    }
}
