package com.smartpayut.payment.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartpayut.payment.domain.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    Optional<Payment> findByPlaceToPayRequestId(Long requestId);

    List<Payment> findAllByUserAccountIdOrderByCreatedAtDesc(UUID userAccountId);

    List<Payment> findAllByOrderByCreatedAtDesc();
}
