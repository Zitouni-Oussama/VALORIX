package com.recyclix.backend.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends ApiException {
    public BusinessException(String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, message, "BUSINESS_ERROR");
    }
}