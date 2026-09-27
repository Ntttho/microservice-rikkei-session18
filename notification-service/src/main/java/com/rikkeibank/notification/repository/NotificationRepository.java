package com.rikkeibank.notification.repository;

import com.rikkeibank.notification.entity.Notification;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface NotificationRepository extends R2dbcRepository<Notification, Long> {
    Flux<Notification> findByRecipientEmailOrderByCreatedAtDesc(String recipientEmail);
    Flux<Notification> findByTransactionReference(String transactionReference);
}
