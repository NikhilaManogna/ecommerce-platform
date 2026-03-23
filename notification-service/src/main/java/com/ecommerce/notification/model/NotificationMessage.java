package com.ecommerce.notification.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class NotificationMessage {
    private Long orderId;
    private Long userId;
    private String message;
    private Instant processedAt;
}
