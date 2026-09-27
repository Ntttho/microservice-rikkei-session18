package com.rikkeibank.transaction.controller;

import com.rikkeibank.transaction.dto.ApiResponse;
import com.rikkeibank.transaction.dto.TransactionResponse;
import com.rikkeibank.transaction.dto.TransferRequest;
import com.rikkeibank.transaction.service.SagaTransferOrchestrator;
import com.rikkeibank.transaction.service.TransactionQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final SagaTransferOrchestrator transferOrchestrator;
    private final TransactionQueryService queryService;

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> executeTransfer(
            @Valid @RequestBody TransferRequest request,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Roles", required = false) String userRoles) {

        TransactionResponse response = transferOrchestrator.executeTransfer(
                request,
                userEmail != null ? userEmail : "customer1@rikkeibank.com",
                userRoles != null ? userRoles : "ROLE_CUSTOMER"
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Transfer executed successfully via Saga Orchestrator", response));
    }

    @GetMapping("/reference/{reference}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getByReference(@PathVariable String reference) {
        return ResponseEntity.ok(ApiResponse.success(queryService.getByReference(reference)));
    }

    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getByAccountNumber(@PathVariable String accountNumber) {
        return ResponseEntity.ok(ApiResponse.success(queryService.getByAccountNumber(accountNumber)));
    }

    @GetMapping("/my-history")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getMyHistory(
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestParam(value = "email", required = false) String paramEmail) {
        String email = userEmail != null && !userEmail.isBlank() ? userEmail : paramEmail;
        return ResponseEntity.ok(ApiResponse.success(queryService.getByUserEmail(email)));
    }

    @GetMapping("/teller/daily")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTellerDailyTransactions(
            @RequestHeader(value = "X-User-Email", required = false) String tellerEmail,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(queryService.getDailyTransactionsByTeller(tellerEmail, date)));
    }

    @GetMapping("/admin/daily")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getDailyTransactions(
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(queryService.getDailyTransactions(date)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getAllTransactions() {
        return ResponseEntity.ok(ApiResponse.success(queryService.getAllTransactions()));
    }
}
