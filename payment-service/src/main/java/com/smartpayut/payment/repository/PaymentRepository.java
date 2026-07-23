package com.smartpayut.payment.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartpayut.payment.domain.entity.Payment;
import com.smartpayut.payment.domain.enumeration.PaymentMethod;
import com.smartpayut.payment.domain.enumeration.PaymentStatus;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    Optional<Payment> findByPlaceToPayRequestId(Long requestId);

    List<Payment> findAllByUserAccountIdOrderByCreatedAtDesc(UUID userAccountId);

    List<Payment> findAllByOrderByCreatedAtDesc();

    List<Payment> findAllByStatusAndMethodIn(PaymentStatus status, List<PaymentMethod> methods);
}
