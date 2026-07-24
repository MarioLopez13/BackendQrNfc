package com.smartpayut.transaction.repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartpayut.transaction.domain.entity.TransactionRecord;
import com.smartpayut.transaction.domain.enumeration.TransactionStatus;
import com.smartpayut.transaction.domain.enumeration.TransactionType;
import com.smartpayut.transaction.repository.projection.DailyOperationCount;
import com.smartpayut.transaction.repository.projection.TransactionMethodCount;
import com.smartpayut.transaction.repository.projection.TransactionStatusCount;

public interface TransactionRecordRepository
        extends JpaRepository<TransactionRecord, UUID>,
        JpaSpecificationExecutor<TransactionRecord> {

    Optional<TransactionRecord> findByCorrelationId(String correlationId);

    Optional<TransactionRecord> findByIdAndUserAccountId(UUID id, UUID userAccountId);

    Page<TransactionRecord> findAllByUserAccountId(UUID userAccountId, Pageable pageable);

    Page<TransactionRecord> findAllByUserAccountIdAndTransactionTypeNot(
            UUID userAccountId,
            TransactionType transactionType,
            Pageable pageable);

    long countByOccurredAtBetween(OffsetDateTime start, OffsetDateTime end);

    @Query("""
            select record.status as status, count(record) as total
            from TransactionRecord record
            where record.occurredAt between :start and :end
            group by record.status
            """)
    List<TransactionStatusCount> countStatuses(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end);

    @Query("""
            select coalesce(sum(record.amount), 0)
            from TransactionRecord record
            where record.status = :status
              and record.occurredAt between :start and :end
            """)
    BigDecimal sumAmountByStatus(
            @Param("status") TransactionStatus status,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end);

    @Query("""
            select upper(record.method) as method, count(record) as total
            from TransactionRecord record
            where record.occurredAt between :start and :end
              and record.method is not null
            group by upper(record.method)
            order by upper(record.method)
            """)
    List<TransactionMethodCount> countMethods(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end);

    @Query(value = """
            select cast(occurred_at as date) as operationDate, count(*) as total
            from transaction_records
            where occurred_at between :start and :end
            group by cast(occurred_at as date)
            order by cast(occurred_at as date)
            """, nativeQuery = true)
    List<DailyOperationCount> countDailyOperations(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end);
}
