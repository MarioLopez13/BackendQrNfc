package com.smartpayut.payment.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.smartpayut.payment.domain.entity.PaymentOutboxEvent;
import com.smartpayut.payment.domain.enumeration.OutboxStatus;

public interface PaymentOutboxEventRepository extends JpaRepository<PaymentOutboxEvent, String> {

    Optional<PaymentOutboxEvent> findByPaymentIdAndEventType(UUID paymentId, String eventType);

    List<PaymentOutboxEvent> findByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
            OutboxStatus status,
            OffsetDateTime nextAttemptAt,
            Pageable pageable);
}
