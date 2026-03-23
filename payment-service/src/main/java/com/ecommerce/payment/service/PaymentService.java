package com.ecommerce.payment.service;

import com.ecommerce.payment.dto.PaymentResponse;
import com.ecommerce.payment.entity.Payment;
import com.ecommerce.payment.entity.PaymentStatus;
import com.ecommerce.payment.event.OrderCreatedEvent;
import com.ecommerce.payment.event.PaymentCompletedEvent;
import com.ecommerce.payment.exception.ApiException;
import com.ecommerce.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate;

    @Transactional
    @KafkaListener(topics = "order-created", groupId = "${spring.kafka.consumer.group-id}")
    public void processOrder(OrderCreatedEvent event) {
        if (paymentRepository.findByOrderId(event.orderId()).isPresent()) {
            log.info("Payment already exists for order {}", event.orderId());
            return;
        }

        Payment payment = new Payment();
        payment.setOrderId(event.orderId());
        payment.setUserId(event.userId());
        payment.setAmount(event.totalAmount());
        payment.setStatus(PaymentStatus.COMPLETED);
        Payment saved = paymentRepository.save(payment);

        PaymentCompletedEvent paymentCompletedEvent = new PaymentCompletedEvent(
                saved.getId(),
                saved.getOrderId(),
                saved.getUserId(),
                saved.getAmount(),
                saved.getStatus().name(),
                Instant.now());
        kafkaTemplate.send("payment-completed", saved.getOrderId().toString(), paymentCompletedEvent);
        log.info("Processed payment {} for order {}", saved.getId(), saved.getOrderId());
    }

    public PaymentResponse getByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ApiException("Payment not found"));
        return new PaymentResponse(payment.getId(), payment.getOrderId(), payment.getUserId(), payment.getAmount(),
                payment.getStatus(), payment.getCreatedAt());
    }
}
