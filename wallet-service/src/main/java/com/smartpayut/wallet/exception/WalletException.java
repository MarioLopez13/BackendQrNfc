package com.smartpayut.wallet.exception;

import org.springframework.http.HttpStatus;

public class WalletException extends RuntimeException {
    private final HttpStatus status;

    public WalletException(HttpStatus s, String m) {
        super(m);
        status = s;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
