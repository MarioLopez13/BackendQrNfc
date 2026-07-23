package com.smartpayut.wallet.dto.response;

public record ApiResponse<T>(boolean success, String message, T data) {
    public static <T> ApiResponse<T> ok(String m, T d) {
        return new ApiResponse<>(true, m, d);
    }

    public static ApiResponse<Void> fail(String m) {
        return new ApiResponse<>(false, m, null);
    }
}
