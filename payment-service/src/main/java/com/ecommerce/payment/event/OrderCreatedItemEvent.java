package com.ecommerce.payment.event;

import java.math.BigDecimal;

public record OrderCreatedItemEvent(
        Long productId,
        Integer quantity,
        BigDecimal price
) {
}
