package com.ecommerce.user.dto;

import java.time.Instant;

public record UserProfileResponse(
        Long id,
        String name,
        String email,
        Instant createdAt
) {
}
