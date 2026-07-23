package com.smartpayut.payment.messaging.publisher;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.smartpayut.payment.domain.enumeration.PaymentMethod;
import com.smartpayut.payment.domain.enumeration.PaymentStatus;
import com.smartpayut.payment.repository.PaymentRepository;

@Component
@ConditionalOnProperty(name = "payment.reconciliation.enabled", havingValue = "true")
public class PaymentEventReconciliationRunner implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentEventReconciliationRunner.class);

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher eventPublisher;

    public PaymentEventReconciliationRunner(
            PaymentRepository paymentRepository,
            PaymentEventPublisher eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        var payments = paymentRepository.findAllByStatusAndMethodIn(
                PaymentStatus.COMPLETED,
                List.of(PaymentMethod.QR, PaymentMethod.NFC));
        payments.forEach(payment -> eventPublisher.publish("payment.completed", payment));
        LOGGER.info("Reconciliación manual preparada para {} pagos completados.", payments.size());
    }
}
