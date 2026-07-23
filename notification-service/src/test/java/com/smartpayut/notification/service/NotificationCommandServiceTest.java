package com.smartpayut.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.smartpayut.notification.domain.entity.Notification;
import com.smartpayut.notification.domain.enumeration.NotificationSource;
import com.smartpayut.notification.domain.enumeration.NotificationStatus;
import com.smartpayut.notification.domain.enumeration.NotificationType;
import com.smartpayut.notification.exception.NotificationNotFoundException;
import com.smartpayut.notification.mapper.NotificationMapper;
import com.smartpayut.notification.repository.NotificationRepository;

@ExtendWith(MockitoExtension.class)
class NotificationCommandServiceTest {

    @Mock
    private NotificationRepository repository;

    private NotificationCommandService service;

    @BeforeEach
    void setUp() {
        service = new NotificationCommandService(repository, new NotificationMapper());
    }

    @Test
    void marksOwnedNotificationAsRead() {
        UUID userId = UUID.randomUUID();
        Notification notification = notification(userId, "event-1");
        when(repository.findByIdAndUserId(notification.getId(), userId)).thenReturn(Optional.of(notification));
        when(repository.save(notification)).thenReturn(notification);
        assertThat(service.markAsRead(userId, notification.getId()).status()).isEqualTo(NotificationStatus.READ);
    }

    @Test
    void preventsMarkingAnotherUsersNotification() {
        UUID notificationId = UUID.randomUUID();
        UUID requestingUser = UUID.randomUUID();
        when(repository.findByIdAndUserId(notificationId, requestingUser)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.markAsRead(requestingUser, notificationId))
                .isInstanceOf(NotificationNotFoundException.class);
    }

    @Test
    void marksAllUsersNotificationsAsRead() {
        UUID userId = UUID.randomUUID();
        List<Notification> notifications = List.of(notification(userId, "event-1"), notification(userId, "event-2"));
        when(repository.findAllByUserIdAndStatus(userId, NotificationStatus.UNREAD)).thenReturn(notifications);
        assertThat(service.markAllAsRead(userId)).isEqualTo(2);
        assertThat(notifications).allMatch(item -> item.getStatus() == NotificationStatus.READ);
        verify(repository).saveAll(notifications);
    }

    private Notification notification(UUID userId, String eventId) {
        return new Notification(eventId, userId, NotificationType.PAYMENT_COMPLETED,
                "Pago", "Pago completado", NotificationSource.PAYMENT, UUID.randomUUID().toString(), null);
    }
}
