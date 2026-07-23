package com.smartpayut.payment.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartpayut.payment.domain.entity.PaymentRefund;

public interface PaymentRefundRepository extends JpaRepository<PaymentRefund, UUID> {

    Optional<PaymentRefund> findByIdempotencyKey(String idempotencyKey);
}
