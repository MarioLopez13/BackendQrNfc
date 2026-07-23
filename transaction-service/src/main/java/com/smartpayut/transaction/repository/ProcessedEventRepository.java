package com.smartpayut.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartpayut.transaction.domain.entity.ProcessedEvent;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String> {
}
