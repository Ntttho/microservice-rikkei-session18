package com.rikkeibank.notification.consumer;

import com.rikkeibank.notification.event.TransactionEvent;
import com.rikkeibank.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "rikkeibank-transaction-events", groupId = "notification-group")
    public void consumeTransactionEvent(TransactionEvent event) {
        log.info("[Kafka Consumer] Received transaction event: {} for Tx: {}", event.getEventType(), event.getTransactionReference());
        notificationService.processTransactionEvent(event).subscribe();
    }
}
