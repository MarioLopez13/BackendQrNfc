package com.smartpayut.transaction.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record DashboardSummaryResponse(
        long totalTransactions,
        long completedTransactions,
        long pendingTransactions,
        long failedTransactions,
        long refundedTransactions,
        BigDecimal approvedAmount,
        Map<String, Long> operationsByMethod,
        List<DailyOperationResponse> dailyOperations) {
}
