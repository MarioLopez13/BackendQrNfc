package com.smartpayut.identity.exception;

import com.smartpayut.identity.dto.response.ApiResponse;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IdentityException.class)
    ResponseEntity<ApiResponse<Void>> identity(IdentityException e) {
        return ResponseEntity.status(e.getStatus()).body(ApiResponse.failure(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> validation(MethodArgumentNotValidException e) {
        String m = e.getBindingResult().getFieldErrors().stream().findFirst()
                .map(x -> x.getField() + ": " + x.getDefaultMessage()).orElse("Datos inválidos");
        return ResponseEntity.badRequest().body(ApiResponse.failure(m));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> unexpected(Exception e) {
        return ResponseEntity.internalServerError().body(ApiResponse.failure("Error interno de identidad"));
    }
}
