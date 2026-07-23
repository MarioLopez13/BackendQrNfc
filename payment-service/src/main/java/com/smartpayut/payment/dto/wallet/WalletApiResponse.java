package com.smartpayut.payment.dto.wallet;

public record WalletApiResponse<T>(boolean success, String message, T data) {
}
