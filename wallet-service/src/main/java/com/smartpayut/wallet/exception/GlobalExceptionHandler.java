package com.smartpayut.wallet.exception;

import com.smartpayut.wallet.dto.response.ApiResponse;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(WalletException.class)
    ResponseEntity<ApiResponse<Void>> wallet(WalletException e) {
        return ResponseEntity.status(e.getStatus()).body(ApiResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> validation(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body(ApiResponse.fail(e.getBindingResult().getFieldErrors().stream()
                .findFirst().map(x -> x.getField() + ": " + x.getDefaultMessage()).orElse("Datos inválidos")));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> unexpected(Exception e) {
        return ResponseEntity.internalServerError().body(ApiResponse.fail("Error interno de Wallet"));
    }
}
