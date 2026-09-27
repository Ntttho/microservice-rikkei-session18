package com.rikkeibank.transaction.service;

import com.rikkeibank.transaction.dto.TransactionResponse;
import com.rikkeibank.transaction.entity.Transaction;
import com.rikkeibank.transaction.exception.ResourceNotFoundException;
import com.rikkeibank.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionQueryService {

    private final TransactionRepository transactionRepository;

    public TransactionResponse getByReference(String reference) {
        Transaction tx = transactionRepository.findByTransactionReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with reference: " + reference));
        return mapToResponse(tx);
    }

    public List<TransactionResponse> getByAccountNumber(String accountNumber) {
        return transactionRepository.findBySourceAccountNumberOrTargetAccountNumberOrderByCreatedAtDesc(accountNumber, accountNumber)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<TransactionResponse> getByUserEmail(String email) {
        return transactionRepository.findByPerformedByEmailOrderByCreatedAtDesc(email)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<TransactionResponse> getDailyTransactions(LocalDate date) {
        LocalDate queryDate = date != null ? date : LocalDate.now();
        LocalDateTime startOfDay = queryDate.atStartOfDay();
        LocalDateTime endOfDay = queryDate.atTime(LocalTime.MAX);

        return transactionRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startOfDay, endOfDay)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<TransactionResponse> getDailyTransactionsByTeller(String tellerEmail, LocalDate date) {
        LocalDate queryDate = date != null ? date : LocalDate.now();
        LocalDateTime startOfDay = queryDate.atStartOfDay();
        LocalDateTime endOfDay = queryDate.atTime(LocalTime.MAX);

        return transactionRepository.findByPerformedByEmailAndCreatedAtBetweenOrderByCreatedAtDesc(tellerEmail, startOfDay, endOfDay)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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
