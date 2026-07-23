package com.smartpayut.payment.controller;

import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartpayut.payment.dto.common.ApiResponse;
import com.smartpayut.payment.dto.request.PlaceToPayCallbackRequest;
import com.smartpayut.payment.dto.request.PlaceToPayTopUpRequest;
import com.smartpayut.payment.dto.response.PaymentResponse;
import com.smartpayut.payment.dto.response.TopUpResponse;
import com.smartpayut.payment.service.PlaceToPayService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments/placetopay")
public class PlaceToPayController {

    private final PlaceToPayService placeToPayService;

    public PlaceToPayController(PlaceToPayService placeToPayService) {
        this.placeToPayService = placeToPayService;
    }

    @PostMapping("/top-ups")
    public ApiResponse<TopUpResponse> create(
            @Valid @RequestBody PlaceToPayTopUpRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken,
            HttpServletRequest httpRequest) {
        TopUpResponse response = placeToPayService.create(
                request,
                idempotencyKey,
                bearerToken,
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader(HttpHeaders.USER_AGENT));
        return ApiResponse.ok("Link de recarga PlaceToPay generado correctamente.", response);
    }

    @PostMapping("/top-ups/{topUpId}/confirm")
    public ApiResponse<PaymentResponse> confirm(@PathVariable UUID topUpId) {
        return ApiResponse.ok(
                "Estado de recarga PlaceToPay actualizado correctamente.",
                placeToPayService.confirm(topUpId));
    }

    @PostMapping("/callback")
    public ResponseEntity<Void> callback(@Valid @RequestBody PlaceToPayCallbackRequest request) {
        placeToPayService.callback(request);
        return ResponseEntity.noContent().build();
    }
}
