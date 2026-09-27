package com.rikkeibank.notification.service;

import com.rikkeibank.notification.entity.Notification;
import com.rikkeibank.notification.event.TransactionEvent;
import com.rikkeibank.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Mono<Notification> createNotification(String recipient, String title, String message, String channel, String txRef) {
        Notification notification = Notification.builder()
                .recipientEmail(recipient)
                .title(title)
                .message(message)
                .channel(channel != null ? channel : "IN_APP")
                .status("SENT")
                .transactionReference(txRef)
                .createdAt(LocalDateTime.now())
                .build();

        return notificationRepository.save(notification)
                .doOnSuccess(n -> log.info("[Notification Sent] ID: {} To: {} Title: {}", n.getId(), n.getRecipientEmail(), n.getTitle()));
    }

    public Flux<Notification> getNotificationsForUser(String recipientEmail) {
        return notificationRepository.findByRecipientEmailOrderByCreatedAtDesc(recipientEmail);
    }

    public Flux<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    public Mono<Void> processTransactionEvent(TransactionEvent event) {
        String eventType = event.getEventType();
        String email = event.getPerformedByEmail() != null ? event.getPerformedByEmail() : "customer1@rikkeibank.com";

        String title;
        String message;

        switch (eventType) {
            case "TRANSFER_COMPLETED":
                title = "Biến động số dư: Chuyển khoản thành công";
                message = String.format("Giao dịch %s thành công. Bạn đã chuyển %,d %s từ TK %s đến TK %s.",
                        event.getTransactionReference(),
                        event.getAmount().longValue(),
                        event.getCurrency(),
                        event.getSourceAccountNumber(),
                        event.getTargetAccountNumber());
                break;

            case "TRANSFER_ROLLED_BACK":
                title = "Cảnh báo giao dịch: Hoàn tiền Saga";
                message = String.format("Giao dịch %s thất bại do lỗi hệ thống đích. Số tiền %,d %s đã được hoàn lại đầy đủ vào TK %s.",
                        event.getTransactionReference(),
                        event.getAmount().longValue(),
                        event.getCurrency(),
                        event.getSourceAccountNumber());
                break;

            case "TRANSFER_FAILED":
                title = "Thông báo: Giao dịch chuyển khoản thất bại";
                message = String.format("Giao dịch %s thất bại. Lý do: %s.",
                        event.getTransactionReference(),
                        event.getReason() != null ? event.getReason() : "Số dư không đủ hoặc tài khoản không hợp lệ");
                break;

            default:
                title = "Thông báo giao dịch RikkeiBank";
                message = String.format("Trạng thái giao dịch %s: %s", event.getTransactionReference(), eventType);
        }

        return createNotification(email, title, message, "IN_APP", event.getTransactionReference())
                .then();
    }
}
