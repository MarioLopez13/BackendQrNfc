package com.smartpayut.notification.mapper;

import org.springframework.stereotype.Component;

import com.smartpayut.notification.domain.entity.Notification;
import com.smartpayut.notification.dto.response.NotificationResponse;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(), notification.getUserId(), notification.getType(),
                notification.getTitle(), notification.getMessage(), notification.getStatus(),
                notification.getSource(), notification.getReferenceId(), notification.getAmount(),
                notification.getReadAt(), notification.getCreatedAt());
    }
}
