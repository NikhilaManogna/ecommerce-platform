package com.ecommerce.order.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderCreatedEvent(
        Long orderId,
        Long userId,
        BigDecimal totalAmount,
        Instant createdAt,
        List<OrderCreatedItemEvent> items
) {
}
