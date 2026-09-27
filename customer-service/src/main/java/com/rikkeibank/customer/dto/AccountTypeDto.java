package com.rikkeibank.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountTypeDto implements Serializable {
    private Long id;

    @NotBlank(message = "Account type code is required")
    private String code;

    @NotBlank(message = "Account type name is required")
    private String name;

    private String description;

    @NotNull(message = "Interest rate is required")
    private BigDecimal interestRate;

    @NotNull(message = "Minimum balance is required")
    private BigDecimal minimumBalance;

    @NotNull(message = "Daily transfer limit is required")
    private BigDecimal dailyTransferLimit;

    @Builder.Default
    private boolean active = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
