package com.smartpayut.transaction.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartpayut.transaction.dto.common.ApiResponse;
import com.smartpayut.transaction.dto.response.DashboardSummaryResponse;
import com.smartpayut.transaction.dto.response.PageResponse;
import com.smartpayut.transaction.dto.response.TransactionResponse;
import com.smartpayut.transaction.service.DashboardQueryService;
import com.smartpayut.transaction.service.TransactionQueryService;

@RestController
@RequestMapping("/api/admin/transactions")
@PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
public class AdminTransactionController {

    private final TransactionQueryService queryService;
    private final DashboardQueryService dashboardQueryService;

    public AdminTransactionController(
            TransactionQueryService queryService,
            DashboardQueryService dashboardQueryService) {
        this.queryService = queryService;
        this.dashboardQueryService = dashboardQueryService;
    }

    @GetMapping
    public ApiResponse<PageResponse<TransactionResponse>> all(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(
                "Historial administrativo consultado correctamente.",
                queryService.all(page, pageSize));
    }

    @GetMapping("/dashboard")
    public ApiResponse<DashboardSummaryResponse> dashboard(
            @RequestParam(defaultValue = "7") int days) {
        return ApiResponse.ok(
                "Resumen del Dashboard obtenido correctamente",
                dashboardQueryService.summary(days));
    }
}
