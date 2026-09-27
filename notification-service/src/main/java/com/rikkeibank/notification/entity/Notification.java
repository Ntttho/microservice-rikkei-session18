package com.rikkeibank.notification.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    private Long id;

    private String recipientEmail;
    private String title;
    private String message;
    private String channel; // IN_APP, EMAIL, SMS
    private String status;  // SENT, READ, PENDING
    private String transactionReference;
    private LocalDateTime createdAt;
}
