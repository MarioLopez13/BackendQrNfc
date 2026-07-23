package com.smartpayut.notification.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.smartpayut.notification.domain.entity.ProcessedEvent;
import com.smartpayut.notification.event.PaymentEvent;
import com.smartpayut.notification.repository.NotificationRepository;
import com.smartpayut.notification.repository.ProcessedEventRepository;
import com.smartpayut.notification.service.InAppNotificationSender;
import com.smartpayut.notification.service.NotificationMessageFactory;
import com.smartpayut.notification.service.NotificationPersistenceService;
import com.smartpayut.notification.service.NotificationProjectionService;
import com.smartpayut.notification.service.ProcessedEventRecorder;

@DataJpaTest
@ActiveProfiles("test")
@Import({
        NotificationProjectionService.class,
        NotificationPersistenceService.class,
        ProcessedEventRecorder.class,
        NotificationMessageFactory.class,
        InAppNotificationSender.class
})
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class NotificationProjectionRollbackIntegrationTest {

    @Autowired
    private NotificationProjectionService projectionService;

    @Autowired
    private NotificationRepository notificationRepository;

    @MockBean
    private ProcessedEventRepository processedEventRepository;

    @Test
    void rollsBackNotificationWhenProcessedEventCannotBeStored() {
        PaymentEvent event = new PaymentEvent("rollback-event", "payment.completed", 1,
                OffsetDateTime.now(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "QR", "COMPLETED", new BigDecimal("0.35"), "USD", null, null, null);
        when(processedEventRepository.existsById("rollback-event")).thenReturn(false);
        when(processedEventRepository.save(any(ProcessedEvent.class)))
                .thenThrow(new IllegalStateException("processed event failure"));

        assertThatThrownBy(() -> projectionService.process(event))
                .isInstanceOf(IllegalStateException.class);

        assertThat(notificationRepository.count()).isZero();
    }
}
