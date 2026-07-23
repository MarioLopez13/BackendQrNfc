package com.smartpayut.notification.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartpayut.notification.domain.entity.Notification;
import com.smartpayut.notification.domain.entity.ProcessedEvent;
import com.smartpayut.notification.repository.NotificationRepository;
import com.smartpayut.notification.repository.ProcessedEventRepository;

@Service
public class NotificationPersistenceService {

    private final NotificationRepository notificationRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final NotificationSender sender;

    public NotificationPersistenceService(
            NotificationRepository notificationRepository,
            ProcessedEventRepository processedEventRepository,
            NotificationSender sender) {
        this.notificationRepository = notificationRepository;
        this.processedEventRepository = processedEventRepository;
        this.sender = sender;
    }

    @Transactional
    public void persist(Notification notification, String eventType) {
        if (processedEventRepository.existsById(notification.getEventId())) {
            return;
        }
        if (!notificationRepository.existsByBusinessKey(notification.getBusinessKey())) {
            sender.send(notification);
        }
        processedEventRepository.save(new ProcessedEvent(notification.getEventId(), eventType));
    }
}
