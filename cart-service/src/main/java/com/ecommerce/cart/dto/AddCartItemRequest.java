package com.ecommerce.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AddCartItemRequest(
        @NotNull(message = "Product id is required")
        Long productId,
        @NotNull(message = "Product name is required")
        String productName,
        @NotNull(message = "Price is required")
        BigDecimal price,
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity
) {
}
