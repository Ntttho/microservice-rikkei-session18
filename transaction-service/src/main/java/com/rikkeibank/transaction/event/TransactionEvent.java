package com.rikkeibank.transaction.event;

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
public class TransactionEvent implements Serializable {
    private String eventType; // TRANSFER_INITIATED, TRANSFER_COMPLETED, TRANSFER_FAILED, TRANSFER_ROLLED_BACK
    private String transactionReference;
    private String sourceAccountNumber;
    private String targetAccountNumber;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String performedByEmail;
    private String reason;
    private LocalDateTime timestamp;
}
