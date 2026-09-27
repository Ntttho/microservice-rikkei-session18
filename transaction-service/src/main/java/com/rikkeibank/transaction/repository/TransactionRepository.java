package com.rikkeibank.transaction.repository;

import com.rikkeibank.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByTransactionReference(String transactionReference);
    List<Transaction> findBySourceAccountNumberOrTargetAccountNumberOrderByCreatedAtDesc(String source, String target);
    List<Transaction> findByPerformedByEmailOrderByCreatedAtDesc(String performedByEmail);
    List<Transaction> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);
    List<Transaction> findByPerformedByEmailAndCreatedAtBetweenOrderByCreatedAtDesc(String email, LocalDateTime start, LocalDateTime end);
}
