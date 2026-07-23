package com.smartpayut.notification.service;

import org.springframework.data.jpa.domain.Specification;

import com.smartpayut.notification.domain.entity.Notification;
import com.smartpayut.notification.dto.request.NotificationFilter;

public final class TestSpecifications {

    private TestSpecifications() {
    }

    public static Specification<Notification> from(NotificationFilter filter) {
        return NotificationSpecifications.from(filter);
    }
}
