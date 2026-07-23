package com.smartpayut.payment.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartpayut.payment.domain.entity.Payment;
import com.smartpayut.payment.messaging.publisher.PaymentEventPublisher;
import com.smartpayut.payment.repository.PaymentRepository;

@Service
public class PaymentEventStateService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher eventPublisher;

    public PaymentEventStateService(
            PaymentRepository paymentRepository,
            PaymentEventPublisher eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Payment complete(
            Payment payment,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter,
            String eventType) {
        payment.complete(balanceBefore, balanceAfter);
        Payment saved = paymentRepository.save(payment);
        eventPublisher.publish(eventType, saved);
        return saved;
    }

    @Transactional
    public Payment fail(Payment payment, String reason, String eventType) {
        payment.fail(reason);
        Payment saved = paymentRepository.save(payment);
        eventPublisher.publish(eventType, saved);
        return saved;
    }

    @Transactional
    public Payment refund(Payment payment) {
        payment.refund();
        Payment saved = paymentRepository.save(payment);
        eventPublisher.publish("payment.refunded", saved);
        return saved;
    }
}
