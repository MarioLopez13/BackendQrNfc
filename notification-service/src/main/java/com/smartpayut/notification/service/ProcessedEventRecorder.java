package com.smartpayut.notification.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.smartpayut.notification.domain.entity.ProcessedEvent;
import com.smartpayut.notification.repository.NotificationRepository;
import com.smartpayut.notification.repository.ProcessedEventRepository;

@Service
public class ProcessedEventRecorder {

    private final NotificationRepository notificationRepository;
    private final ProcessedEventRepository processedEventRepository;

    public ProcessedEventRecorder(
            NotificationRepository notificationRepository,
            ProcessedEventRepository processedEventRepository) {
        this.notificationRepository = notificationRepository;
        this.processedEventRepository = processedEventRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean recordDuplicate(
            String eventId,
            String eventType,
            String businessKey) {
        boolean eventAlreadyProcessed = processedEventRepository.existsById(eventId);
        boolean businessAlreadyNotified = notificationRepository.existsByBusinessKey(businessKey);
        if (!eventAlreadyProcessed && !businessAlreadyNotified) {
            return false;
        }
        if (!eventAlreadyProcessed) {
            processedEventRepository.save(new ProcessedEvent(eventId, eventType));
        }
        return true;
    }

    @Transactional
    public void recordIgnored(String eventId, String eventType) {
        if (!processedEventRepository.existsById(eventId)) {
            processedEventRepository.save(new ProcessedEvent(eventId, eventType));
        }
    }
}
