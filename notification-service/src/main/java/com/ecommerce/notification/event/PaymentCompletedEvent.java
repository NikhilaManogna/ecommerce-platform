package com.ecommerce.notification.event;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentCompletedEvent(
        Long paymentId,
        Long orderId,
        Long userId,
        BigDecimal amount,
        String status,
        Instant processedAt
) {
}
