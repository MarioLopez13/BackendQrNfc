package com.smartpayut.transaction.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.smartpayut.transaction.domain.entity.TransactionRecord;

public interface TransactionRecordRepository extends JpaRepository<TransactionRecord, UUID> {

    Optional<TransactionRecord> findByCorrelationId(String correlationId);

    Optional<TransactionRecord> findByIdAndUserAccountId(UUID id, UUID userAccountId);

    Page<TransactionRecord> findAllByUserAccountId(UUID userAccountId, Pageable pageable);
}
