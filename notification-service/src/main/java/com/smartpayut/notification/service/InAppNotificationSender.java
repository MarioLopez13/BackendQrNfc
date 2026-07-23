package com.smartpayut.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.smartpayut.notification.domain.entity.Notification;
import com.smartpayut.notification.repository.NotificationRepository;

@Component
public class InAppNotificationSender implements NotificationSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(InAppNotificationSender.class);

    private final NotificationRepository repository;

    public InAppNotificationSender(NotificationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Notification send(Notification notification) {
        Notification saved = repository.save(notification);
        LOGGER.info("Notificación in-app {} creada para usuario {}.", saved.getType(), saved.getUserId());
        return saved;
    }
}
