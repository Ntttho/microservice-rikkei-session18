package com.rikkeibank.customer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "account_types")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountType implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code; // CHECKING, SAVINGS, VIP, PAYROLL

    @Column(nullable = false)
    private String name;

    private String description;

    @Builder.Default
    private BigDecimal interestRate = BigDecimal.ZERO; // e.g. 0.05 (5% / year)

    @Builder.Default
    private BigDecimal minimumBalance = BigDecimal.valueOf(50000); // 50k VND

    @Builder.Default
    private BigDecimal dailyTransferLimit = BigDecimal.valueOf(500000000); // 500M VND

    @Builder.Default
    private boolean active = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
