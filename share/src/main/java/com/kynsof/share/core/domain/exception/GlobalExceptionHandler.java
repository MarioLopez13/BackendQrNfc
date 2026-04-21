package com.kynsof.share.core.domain.exception;

import com.kynsof.share.core.domain.response.ApiError;
import com.kynsof.share.core.domain.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpClientErrorException(HttpClientErrorException ex) {
        HttpStatus status = (HttpStatus) ex.getStatusCode();
        ApiError apiError = new ApiError("HTTP Error: " + ex.getMessage(), null);
        ApiResponse<?> apiResponse = ApiResponse.fail(apiError);
        return ResponseEntity.status(status).body(apiResponse);
    }

    @ExceptionHandler(CustomUnauthorizedException.class)
    public ResponseEntity<ApiResponse<?>> handleCustomUnauthorizedException(CustomUnauthorizedException ex) {
        ApiError apiError = new ApiError("An unexpected null value was encountered.", null);
        ApiResponse<?> apiResponse = ApiResponse.fail(apiError);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiResponse);
    }

    @ExceptionHandler(AuthenticateNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleAuthenticateNotFoundException(AuthenticateNotFoundException ex) {
        ApiError apiError = new ApiError(ex.getMessage(),
                List.of(ex.getErrorField()));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail(apiError));
    }

    @ExceptionHandler(UserChangePasswordException.class)
    public ResponseEntity<ApiResponse<?>> handleUserChangePasswordException(UserChangePasswordException ex) {
        ApiError apiError = new ApiError(ex.getMessage(),
                List.of(ex.getErrorField()));
        return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED).body(ApiResponse.fail(apiError));
    }

    @ExceptionHandler(BusinessNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessNotFoundException(BusinessNotFoundException ex) {
        ApiError apiError = new ApiError(ex.getStatus(), ex.getMessage(),
                List.of(ex.getBrokenRule().getErrorField()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail(apiError));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessException(BusinessException ex) {
        ApiError apiError = new ApiError(ex.getStatus(), ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail(apiError));
    }

    @ExceptionHandler(DoctorIsNotPermisionNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessException(DoctorIsNotPermisionNotFoundException ex) {
        ApiError apiError = new ApiError(ex.getStatus(), ex.getMessage(),
                List.of(ex.getBrokenRule().getErrorField()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail(apiError));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleUserNotFoundException(UserNotFoundException ex) {
        ApiError apiError = new ApiError(ex.getMessage(),
                List.of(ex.getErrorField()));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail(apiError));
    }

    @ExceptionHandler(javax.ws.rs.NotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFoundException(javax.ws.rs.NotFoundException ex) {
        ApiError apiError = new ApiError(ex.getMessage(), null);
        ApiResponse<?> apiResponse = ApiResponse.fail(apiError);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<?>> handleNullPointerException(NullPointerException ex) {
        ApiError apiError = new ApiError("An unexpected null value was encountered.", null);
        ApiResponse<?> apiResponse = ApiResponse.fail(apiError);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
    }

    @ExceptionHandler(UserEmailDifferentException.class)
    public ResponseEntity<ApiResponse<?>> handleUserEmailDifferentException(UserEmailDifferentException ex) {
        ApiError apiError = new ApiError(ex.getMessage(),
                List.of(ex.getErrorField()));
        return ResponseEntity.status(555).body(ApiResponse.fail(apiError));
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidOtpException(InvalidOtpException ex) {
        ApiError apiError = new ApiError(ex.getStatus(), ex.getMessage(),
                List.of(ex.getErrorField()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail(apiError));
    }

    @ExceptionHandler(BusinessRuleValidationException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessRuleValidationException(BusinessRuleValidationException ex) {
        ApiError apiError = new ApiError(ex.getStatus(), ex.getMessage(),
                List.of(ex.getBrokenRule().getErrorField()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail(apiError));
    }
}
