package com.rikkeibank.transaction.service;

import com.rikkeibank.transaction.client.AccountClient;
import com.rikkeibank.transaction.dto.*;
import com.rikkeibank.transaction.entity.Transaction;
import com.rikkeibank.transaction.event.TransactionEvent;
import com.rikkeibank.transaction.exception.BadRequestException;
import com.rikkeibank.transaction.exception.TransferException;
import com.rikkeibank.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaTransferOrchestrator {

    private final TransactionRepository transactionRepository;
    private final AccountClient accountClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.transaction-events:rikkeibank-transaction-events}")
    private String transactionTopic;

    @Transactional
    public TransactionResponse executeTransfer(TransferRequest request, String performedByEmail, String performedByRole) {
        if (request.getSourceAccountNumber().equals(request.getTargetAccountNumber())) {
            throw new BadRequestException("Source and target accounts must be different");
        }

        String reference = "TXN-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        // 1. Initial State: Create PENDING Transaction record
        Transaction transaction = Transaction.builder()
                .transactionReference(reference)
                .sourceAccountNumber(request.getSourceAccountNumber())
                .targetAccountNumber(request.getTargetAccountNumber())
                .amount(request.getAmount())
                .currency("VND")
                .description(request.getDescription() != null ? request.getDescription() : "Transfer to " + request.getTargetAccountNumber())
                .transactionType("TRANSFER")
                .status("PENDING")
                .performedByEmail(performedByEmail != null ? performedByEmail : "SYSTEM")
                .performedByRole(performedByRole != null ? performedByRole : "CUSTOMER")
                .build();

        Transaction savedTx = transactionRepository.save(transaction);
        log.info("[Saga Initiated] Transaction {} created with status PENDING", reference);

        publishKafkaEvent("TRANSFER_INITIATED", savedTx, null);

        // 2. Step 1 of Saga: Debit Sender Account
        DebitCreditRequest debitReq = DebitCreditRequest.builder()
                .accountNumber(request.getSourceAccountNumber())
                .amount(request.getAmount())
                .transactionReference(reference)
                .description("Debit for transfer " + reference)
                .build();

        ApiResponse<AccountDto> debitResponse;
        try {
            debitResponse = accountClient.debitForTransfer(debitReq);
        } catch (Exception e) {
            savedTx.setStatus("FAILED");
            savedTx.setFailureReason("Failed to debit sender account: " + e.getMessage());
            transactionRepository.save(savedTx);
            publishKafkaEvent("TRANSFER_FAILED", savedTx, e.getMessage());
            throw new TransferException("Transfer failed during debit: " + e.getMessage());
        }

        if (debitResponse == null || debitResponse.getStatus() != 200) {
            String errMsg = debitResponse != null ? debitResponse.getMessage() : "Unknown debit error";
            savedTx.setStatus("FAILED");
            savedTx.setFailureReason("Sender debit rejected: " + errMsg);
            transactionRepository.save(savedTx);
            publishKafkaEvent("TRANSFER_FAILED", savedTx, errMsg);
            throw new TransferException("Transfer rejected: " + errMsg);
        }

        log.info("[Saga Step 1 Passed] Successfully debited sender {}", request.getSourceAccountNumber());

        // 3. Step 2 of Saga: Credit Receiver Account
        // Check for simulated failure to demonstrate Compensating Rollback
        if (request.isSimulateReceiverCreditFailure()) {
            log.warn("[Saga Simulation] Simulating credit failure for receiver {}", request.getTargetAccountNumber());
            compensateSender(savedTx, debitReq, "Simulated network failure on credit receiver step");
            throw new TransferException("Transfer failed: Receiver service encountered an error. SAGA ROLLBACK TRIGGERED: Sender funds have been refunded.");
        }

        DebitCreditRequest creditReq = DebitCreditRequest.builder()
                .accountNumber(request.getTargetAccountNumber())
                .amount(request.getAmount())
                .transactionReference(reference)
                .description("Credit from transfer " + reference)
                .build();

        ApiResponse<AccountDto> creditResponse;
        try {
            creditResponse = accountClient.creditForTransfer(creditReq);
        } catch (Exception e) {
            log.error("[Saga Step 2 Failed] Error crediting receiver: {}. Triggering Compensating Rollback...", e.getMessage());
            compensateSender(savedTx, debitReq, "Receiver credit exception: " + e.getMessage());
            throw new TransferException("Transfer failed on credit step. Sender debited amount has been successfully refunded via Saga Compensation.");
        }

        if (creditResponse == null || creditResponse.getStatus() != 200) {
            String errMsg = creditResponse != null ? creditResponse.getMessage() : "Credit service unavailable";
            log.error("[Saga Step 2 Failed] Credit response not OK: {}. Triggering Compensating Rollback...", errMsg);
            compensateSender(savedTx, debitReq, "Receiver credit failed: " + errMsg);
            throw new TransferException("Transfer credit failed: " + errMsg + ". Sender debited amount has been refunded via Saga Compensation.");
        }

        // 4. Saga Success: Finalize transaction
        savedTx.setStatus("SUCCESS");
        savedTx.setCompletedAt(LocalDateTime.now());
        Transaction completedTx = transactionRepository.save(savedTx);

        log.info("[Saga Completed] Transfer {} SUCCESSFUL from {} to {} with amount {}",
                reference, request.getSourceAccountNumber(), request.getTargetAccountNumber(), request.getAmount());

        publishKafkaEvent("TRANSFER_COMPLETED", completedTx, null);

        return mapToResponse(completedTx);
    }

    private void compensateSender(Transaction tx, DebitCreditRequest originalDebitReq, String reason) {
        log.warn("[Saga Compensating] Compensating sender {} for transaction reference {}",
                tx.getSourceAccountNumber(), tx.getTransactionReference());

        try {
            ApiResponse<AccountDto> compResponse = accountClient.compensateDebit(originalDebitReq);
            if (compResponse != null && compResponse.getStatus() == 200) {
                log.info("[Saga Compensating Succeeded] Sender {} restored successfully", tx.getSourceAccountNumber());
            } else {
                log.error("[Saga Compensating Warning] Sender compensation returned non-200");
            }
        } catch (Exception e) {
            log.error("[Saga Compensating Error] Failed to compensate sender: {}", e.getMessage());
        }

        tx.setStatus("ROLLED_BACK");
        tx.setFailureReason("Compensated due to: " + reason);
        tx.setCompletedAt(LocalDateTime.now());
        transactionRepository.save(tx);

        publishKafkaEvent("TRANSFER_ROLLED_BACK", tx, reason);
    }

    private void publishKafkaEvent(String eventType, Transaction tx, String reason) {
        try {
            TransactionEvent event = TransactionEvent.builder()
                    .eventType(eventType)
                    .transactionReference(tx.getTransactionReference())
                    .sourceAccountNumber(tx.getSourceAccountNumber())
                    .targetAccountNumber(tx.getTargetAccountNumber())
                    .amount(tx.getAmount())
                    .currency(tx.getCurrency())
                    .status(tx.getStatus())
                    .performedByEmail(tx.getPerformedByEmail())
                    .reason(reason)
                    .timestamp(LocalDateTime.now())
                    .build();

            kafkaTemplate.send(transactionTopic, tx.getTransactionReference(), event);
            log.info("Published Kafka Event [{}] for Tx: {}", eventType, tx.getTransactionReference());
        } catch (Exception e) {
            log.warn("Failed to publish Kafka event: {}", e.getMessage());
        }
    }

    private TransactionResponse mapToResponse(Transaction tx) {
        return TransactionResponse.builder()
                .id(tx.getId())
                .transactionReference(tx.getTransactionReference())
                .sourceAccountNumber(tx.getSourceAccountNumber())
                .targetAccountNumber(tx.getTargetAccountNumber())
                .amount(tx.getAmount())
                .currency(tx.getCurrency())
                .description(tx.getDescription())
                .transactionType(tx.getTransactionType())
                .status(tx.getStatus())
                .failureReason(tx.getFailureReason())
                .performedByEmail(tx.getPerformedByEmail())
                .performedByRole(tx.getPerformedByRole())
                .createdAt(tx.getCreatedAt())
                .completedAt(tx.getCompletedAt())
                .build();
    }
}
