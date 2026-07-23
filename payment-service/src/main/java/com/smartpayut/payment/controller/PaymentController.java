package com.smartpayut.payment.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartpayut.payment.domain.enumeration.PaymentMethod;
import com.smartpayut.payment.dto.common.ApiResponse;
import com.smartpayut.payment.dto.request.PaymentRequest;
import com.smartpayut.payment.dto.request.RefundRequest;
import com.smartpayut.payment.dto.response.PaymentResponse;
import com.smartpayut.payment.dto.response.RefundResponse;
import com.smartpayut.payment.service.PaymentExecutionService;
import com.smartpayut.payment.service.PaymentQueryService;
import com.smartpayut.payment.service.RefundService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentExecutionService executionService;
    private final PaymentQueryService queryService;
    private final RefundService refundService;

    public PaymentController(
            PaymentExecutionService executionService,
            PaymentQueryService queryService,
            RefundService refundService) {
        this.executionService = executionService;
        this.queryService = queryService;
        this.refundService = refundService;
    }

    @PostMapping("/qr")
    public ApiResponse<PaymentResponse> payWithQr(
            @Valid @RequestBody PaymentRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken) {
        PaymentResponse response = executionService.execute(
                request, PaymentMethod.QR, idempotencyKey, bearerToken);
        return ApiResponse.ok("Pago QR procesado correctamente.", response);
    }

    @PostMapping("/nfc")
    public ApiResponse<PaymentResponse> payWithNfc(
            @Valid @RequestBody PaymentRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken) {
        PaymentResponse response = executionService.execute(
                request, PaymentMethod.NFC, idempotencyKey, bearerToken);
        return ApiResponse.ok("Pago NFC procesado correctamente.", response);
    }

    @GetMapping("/{paymentId}")
    public ApiResponse<PaymentResponse> byId(@PathVariable UUID paymentId) {
        return ApiResponse.ok("Pago consultado correctamente.", queryService.byId(paymentId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ApiResponse<List<PaymentResponse>> all() {
        return ApiResponse.ok("Pagos consultados correctamente.", queryService.all());
    }

    @PostMapping("/{paymentId}/refunds")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ApiResponse<RefundResponse> refund(
            @PathVariable UUID paymentId,
            @Valid @RequestBody RefundRequest request) {
        return ApiResponse.ok("Pago reembolsado correctamente.", refundService.refund(paymentId, request));
    }
}
