package com.ecommerce.order.exception;

public class ApiException extends RuntimeException {

    public ApiException(String message) {
        super(message);
    }
}
