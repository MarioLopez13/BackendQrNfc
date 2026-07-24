package com.smartpayut.payment.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
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
import com.smartpayut.payment.dto.request.PlaceToPayTopUpRequest;
import com.smartpayut.payment.dto.response.PaymentResponse;
import com.smartpayut.payment.dto.response.TopUpResponse;
import com.smartpayut.payment.service.PaymentExecutionService;
import com.smartpayut.payment.service.PaymentQueryService;
import com.smartpayut.payment.service.PlaceToPayService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/mobile-payments")
public class LegacyMobilePaymentController {

    private final PaymentExecutionService executionService;
    private final PaymentQueryService queryService;
    private final PlaceToPayService placeToPayService;

    public LegacyMobilePaymentController(
            PaymentExecutionService executionService,
            PaymentQueryService queryService,
            PlaceToPayService placeToPayService) {
        this.executionService = executionService;
        this.queryService = queryService;
        this.placeToPayService = placeToPayService;
    }

    @PostMapping("/qr")
    public ApiResponse<PaymentResponse> qr(
            @Valid @RequestBody PaymentRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken) {
        return ApiResponse.ok(
                "Pago procesado correctamente.",
                executionService.execute(request, PaymentMethod.QR, idempotencyKey, bearerToken));
    }

    @PostMapping("/nfc")
    public ApiResponse<PaymentResponse> nfc(
            @Valid @RequestBody PaymentRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken) {
        return ApiResponse.ok(
                "Pago NFC procesado correctamente.",
                executionService.execute(request, PaymentMethod.NFC, idempotencyKey, bearerToken));
    }

    @PostMapping("/top-up/placetopay")
    public ApiResponse<TopUpResponse> topUp(
            @Valid @RequestBody PlaceToPayTopUpRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                "Link de recarga PlaceToPay generado correctamente.",
                placeToPayService.create(
                        request,
                        idempotencyKey,
                        bearerToken,
                        httpRequest.getRemoteAddr(),
                        httpRequest.getHeader(HttpHeaders.USER_AGENT)));
    }

    @PostMapping("/top-up/{topUpId}/confirm")
    public ApiResponse<PaymentResponse> confirm(
            @PathVariable UUID topUpId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken) {
        return ApiResponse.ok(
                "Estado de recarga PlaceToPay consultado correctamente.",
                placeToPayService.confirm(topUpId, bearerToken));
    }

    @GetMapping
    public ApiResponse<List<PaymentResponse>> all() {
        return ApiResponse.ok("Transacciones consultadas correctamente.", queryService.all());
    }

    @GetMapping("/{transactionId}")
    public ApiResponse<PaymentResponse> byId(@PathVariable UUID transactionId) {
        return ApiResponse.ok("TransacciÃ³n consultada correctamente.", queryService.byId(transactionId));
    }
}
