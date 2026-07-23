package com.smartpayut.notification.service;

import com.smartpayut.notification.domain.entity.Notification;

public interface NotificationSender {

    Notification send(Notification notification);
}
