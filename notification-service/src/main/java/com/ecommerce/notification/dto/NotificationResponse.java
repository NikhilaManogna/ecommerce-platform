package com.ecommerce.notification.dto;

import java.time.Instant;

public record NotificationResponse(
        Long orderId,
        Long userId,
        String message,
        Instant processedAt
) {
}
