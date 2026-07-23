package com.smartpayut.transaction.dto.response;

import java.time.LocalDate;

public record DailyOperationResponse(LocalDate date, long count) {
}
