package com.smartpayut.notification.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.smartpayut.notification.domain.entity.Notification;
import com.smartpayut.notification.domain.enumeration.NotificationStatus;

public interface NotificationRepository
        extends JpaRepository<Notification, UUID>, JpaSpecificationExecutor<Notification> {

    Optional<Notification> findByIdAndUserId(UUID id, UUID userId);

    long countByUserIdAndStatus(UUID userId, NotificationStatus status);

    long countByUserIdAndStatusNot(UUID userId, NotificationStatus status);

    java.util.List<Notification> findAllByUserIdAndStatus(UUID userId, NotificationStatus status);
}
