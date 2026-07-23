package com.smartpayut.notification.validator;

import org.springframework.stereotype.Component;

@Component
public class PaginationValidator {

    public void validate(int page, int pageSize) {
        if (page < 0) {
            throw new IllegalArgumentException("page no puede ser negativo.");
        }
        if (pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException("pageSize debe estar entre 1 y 100.");
        }
    }
}
