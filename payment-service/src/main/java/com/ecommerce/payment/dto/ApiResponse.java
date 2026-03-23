package com.ecommerce.payment.dto;

import java.time.Instant;

public record ApiResponse<T>(
        Instant timestamp,
        int status,
        String message,
        T data,
        Object error
) {
    public static <T> ApiResponse<T> success(int status, String message, T data) {
        return new ApiResponse<>(Instant.now(), status, message, data, null);
    }

    public static <T> ApiResponse<T> failure(int status, String message, Object error) {
        return new ApiResponse<>(Instant.now(), status, message, null, error);
    }
}
