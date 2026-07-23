package com.smartpayut.notification.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartpayut.notification.domain.entity.Notification;
import com.smartpayut.notification.domain.enumeration.NotificationStatus;
import com.smartpayut.notification.dto.response.NotificationResponse;
import com.smartpayut.notification.exception.NotificationNotFoundException;
import com.smartpayut.notification.mapper.NotificationMapper;
import com.smartpayut.notification.repository.NotificationRepository;

@Service
public class NotificationCommandService {

    private final NotificationRepository repository;
    private final NotificationMapper mapper;

    public NotificationCommandService(NotificationRepository repository, NotificationMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public NotificationResponse markAsRead(UUID userId, UUID notificationId) {
        Notification notification = repository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new NotificationNotFoundException("Notificación no encontrada."));
        notification.markAsRead();
        return mapper.toResponse(repository.save(notification));
    }

    @Transactional
    public long markAllAsRead(UUID userId) {
        List<Notification> notifications = repository.findAllByUserIdAndStatus(userId, NotificationStatus.UNREAD);
        notifications.forEach(Notification::markAsRead);
        repository.saveAll(notifications);
        return notifications.size();
    }
}
