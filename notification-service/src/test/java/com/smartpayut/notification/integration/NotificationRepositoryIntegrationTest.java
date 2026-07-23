package com.smartpayut.notification.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.smartpayut.notification.domain.entity.Notification;
import com.smartpayut.notification.domain.enumeration.NotificationSource;
import com.smartpayut.notification.domain.enumeration.NotificationStatus;
import com.smartpayut.notification.domain.enumeration.NotificationType;
import com.smartpayut.notification.dto.request.NotificationFilter;
import com.smartpayut.notification.repository.NotificationRepository;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class NotificationRepositoryIntegrationTest {

    @Autowired
    private NotificationRepository repository;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void persistsNotification() {
        Notification saved = repository.saveAndFlush(notification("event-1", userId));
        assertThat(repository.findById(saved.getId())).isPresent();
    }

    @Test
    void enforcesUniqueEventId() {
        repository.saveAndFlush(notification("duplicate-event", userId));
        assertThatThrownBy(() -> repository.saveAndFlush(notification("duplicate-event", UUID.randomUUID())))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void enforcesUniqueBusinessKey() {
        String referenceId = UUID.randomUUID().toString();
        repository.saveAndFlush(notification("original-event", userId, referenceId));

        assertThatThrownBy(() -> repository.saveAndFlush(
                notification("reconciled-event", userId, referenceId)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void paginatesByUserId() {
        repository.saveAndFlush(notification("event-1", userId));
        repository.saveAndFlush(notification("event-2", userId));
        repository.saveAndFlush(notification("event-3", UUID.randomUUID()));
        NotificationFilter filter = new NotificationFilter(userId, null, null, null, null, null);
        assertThat(repository.findAll(com.smartpayut.notification.service.TestSpecifications.from(filter),
                PageRequest.of(0, 20)).getTotalElements()).isEqualTo(2);
    }

    @Test
    void filtersReadAndUnread() {
        Notification read = notification("read-event", userId);
        read.markAsRead();
        repository.saveAndFlush(read);
        repository.saveAndFlush(notification("unread-event", userId));
        NotificationFilter readFilter = new NotificationFilter(
                userId, null, null, NotificationStatus.READ, null, null);
        NotificationFilter unreadFilter = new NotificationFilter(
                userId, null, null, NotificationStatus.UNREAD, null, null);
        assertThat(repository.findAll(com.smartpayut.notification.service.TestSpecifications.from(readFilter)))
                .hasSize(1);
        assertThat(repository.findAll(com.smartpayut.notification.service.TestSpecifications.from(unreadFilter)))
                .hasSize(1);
    }

    @Test
    void countsUnreadNotifications() {
        repository.saveAndFlush(notification("event-1", userId));
        repository.saveAndFlush(notification("event-2", userId));
        assertThat(repository.countByUserIdAndStatus(userId, NotificationStatus.UNREAD)).isEqualTo(2);
    }

    private Notification notification(String eventId, UUID recipient) {
        return notification(eventId, recipient, UUID.randomUUID().toString());
    }

    private Notification notification(String eventId, UUID recipient, String referenceId) {
        return new Notification(eventId, recipient, NotificationType.PAYMENT_COMPLETED,
                "Pago", "Pago completado", NotificationSource.PAYMENT, referenceId, null);
    }
}
