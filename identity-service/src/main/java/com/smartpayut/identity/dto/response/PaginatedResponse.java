package com.smartpayut.identity.dto.response;

import java.util.List;

public record PaginatedResponse<T>(List<T> items, long totalCount, int page, int pageSize, int totalPages) {
}
