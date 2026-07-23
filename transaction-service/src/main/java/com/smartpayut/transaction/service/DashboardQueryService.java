package com.smartpayut.transaction.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartpayut.transaction.domain.enumeration.TransactionStatus;
import com.smartpayut.transaction.dto.response.DailyOperationResponse;
import com.smartpayut.transaction.dto.response.DashboardSummaryResponse;
import com.smartpayut.transaction.repository.TransactionRecordRepository;
import com.smartpayut.transaction.repository.projection.DailyOperationCount;

@Service
@Transactional(readOnly = true)
public class DashboardQueryService {

    private static final int MIN_DAYS = 1;
    private static final int MAX_DAYS = 90;

    private final TransactionRecordRepository repository;

    public DashboardQueryService(TransactionRecordRepository repository) {
        this.repository = repository;
    }

    public DashboardSummaryResponse summary(int days) {
        validateDays(days);

        OffsetDateTime end = OffsetDateTime.now(ZoneOffset.UTC);
        LocalDate startDate = end.toLocalDate().minusDays(days - 1L);
        OffsetDateTime start = startDate.atStartOfDay().atOffset(ZoneOffset.UTC);

        Map<TransactionStatus, Long> statusCounts = new EnumMap<>(TransactionStatus.class);
        repository.countStatuses(start, end)
                .forEach(item -> statusCounts.put(item.getStatus(), item.getTotal()));

        Map<String, Long> methodCounts = repository.countMethods(start, end).stream()
                .collect(Collectors.toMap(
                        item -> item.getMethod().toUpperCase(),
                        item -> item.getTotal(),
                        Long::sum,
                        LinkedHashMap::new));

        Map<LocalDate, Long> dailyCounts = repository.countDailyOperations(start, end).stream()
                .collect(Collectors.toMap(
                        item -> item.getOperationDate().toLocalDate(),
                        DailyOperationCount::getTotal));

        List<DailyOperationResponse> dailyOperations = startDate.datesUntil(end.toLocalDate().plusDays(1))
                .map(date -> new DailyOperationResponse(date, dailyCounts.getOrDefault(date, 0L)))
                .toList();

        BigDecimal approvedAmount = repository.sumAmountByStatus(TransactionStatus.COMPLETED, start, end);

        return new DashboardSummaryResponse(
                repository.countByOccurredAtBetween(start, end),
                statusCounts.getOrDefault(TransactionStatus.COMPLETED, 0L),
                statusCounts.getOrDefault(TransactionStatus.PENDING, 0L),
                statusCounts.getOrDefault(TransactionStatus.FAILED, 0L),
                statusCounts.getOrDefault(TransactionStatus.REFUNDED, 0L),
                approvedAmount == null ? BigDecimal.ZERO : approvedAmount,
                methodCounts,
                dailyOperations);
    }

    private void validateDays(int days) {
        if (days < MIN_DAYS || days > MAX_DAYS) {
            throw new IllegalArgumentException("days debe estar entre 1 y 90.");
        }
    }
}
