package com.ecommerce.notification.service;

import com.ecommerce.notification.dto.NotificationResponse;
import com.ecommerce.notification.event.PaymentCompletedEvent;
import com.ecommerce.notification.model.NotificationMessage;
import com.ecommerce.notification.repository.NotificationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @KafkaListener(topics = "payment-completed", groupId = "${spring.kafka.consumer.group-id}")
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        String message = "Payment completed for order %d and user %d. Amount: %s".formatted(
                event.orderId(), event.userId(), event.amount());
        notificationRepository.save(new NotificationMessage(event.orderId(), event.userId(), message, event.processedAt()));
        log.info("Notification sent: {}", message);
    }

    public List<NotificationResponse> getNotifications() {
        return notificationRepository.findAll().stream()
                .map(message -> new NotificationResponse(
                        message.getOrderId(),
                        message.getUserId(),
                        message.getMessage(),
                        message.getProcessedAt()))
                .toList();
    }
}
