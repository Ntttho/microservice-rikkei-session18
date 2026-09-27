package com.rikkeibank.transaction.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String transactionReference; // e.g. "TXN-20260927-10001"

    @Column(nullable = false)
    private String sourceAccountNumber;

    @Column(nullable = false)
    private String targetAccountNumber;

    @Column(nullable = false)
    private BigDecimal amount;

    @Builder.Default
    private String currency = "VND";

    private String description;

    @Column(nullable = false)
    private String transactionType; // TRANSFER, DEPOSIT, WITHDRAWAL

    @Column(nullable = false)
    private String status; // PENDING, SUCCESS, FAILED, ROLLED_BACK

    private String failureReason;

    private String performedByEmail; // Customer or Teller Email
    private String performedByRole; // CUSTOMER, TELLER, ADMIN

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
