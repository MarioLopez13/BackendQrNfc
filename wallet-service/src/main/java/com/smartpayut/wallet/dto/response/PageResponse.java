package com.smartpayut.wallet.dto.response;

import java.util.List;

public record PageResponse<T>(List<T> items, long totalCount, int page, int pageSize, int totalPages) {
}
