package com.ecommerce.user.dto;

public record AuthResponse(
        String token,
        UserProfileResponse user
) {
}
