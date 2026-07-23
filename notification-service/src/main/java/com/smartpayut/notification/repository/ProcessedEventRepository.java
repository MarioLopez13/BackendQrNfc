package com.smartpayut.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartpayut.notification.domain.entity.ProcessedEvent;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String> {
}
