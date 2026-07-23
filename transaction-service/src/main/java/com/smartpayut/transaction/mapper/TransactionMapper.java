package com.smartpayut.transaction.mapper;

import org.springframework.stereotype.Component;

import com.smartpayut.transaction.domain.entity.TransactionRecord;
import com.smartpayut.transaction.dto.response.TransactionResponse;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(TransactionRecord record) {
        return new TransactionResponse(
                record.getId(),
                record.getCorrelationId(),
                record.getSourceType(),
                record.getSourceId(),
                record.getUserAccountId(),
                record.getWalletId(),
                record.getTransactionType(),
                record.getMethod(),
                record.getStatus(),
                record.getAmount(),
                record.getBalanceBefore(),
                record.getBalanceAfter(),
                record.getCurrency(),
                record.getBusCode(),
                record.getRouteName(),
                record.getFailureReason(),
                record.getOccurredAt());
    }
}
