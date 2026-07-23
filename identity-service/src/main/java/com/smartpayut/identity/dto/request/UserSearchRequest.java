package com.smartpayut.identity.dto.request;

import jakarta.validation.constraints.*;
import java.util.List;

public record UserSearchRequest(List<Object> filter, String query, @Min(0) Integer page,
        @Min(1) @Max(200) Integer pageSize) {
    public int resolvedPage() {
        return page == null ? 0 : page;
    }

    public int resolvedSize() {
        return pageSize == null ? 20 : pageSize;
    }
}
