package com.smartpayut.payment.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.smartpayut.payment.domain.entity.Payment;
import com.smartpayut.payment.dto.response.PaymentResponse;
import com.smartpayut.payment.exception.PaymentNotFoundException;
import com.smartpayut.payment.mapper.PaymentMapper;
import com.smartpayut.payment.repository.PaymentRepository;

@Service
public class PaymentQueryService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper mapper;

    public PaymentQueryService(PaymentRepository paymentRepository, PaymentMapper mapper) {
        this.paymentRepository = paymentRepository;
        this.mapper = mapper;
    }

    public PaymentResponse byId(UUID paymentId) {
        return mapper.toResponse(required(paymentId));
    }

    public List<PaymentResponse> all() {
        return paymentRepository.findAllByOrderByCreatedAtDesc().stream().map(mapper::toResponse).toList();
    }

    Payment required(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Pago no encontrado."));
    }
}
