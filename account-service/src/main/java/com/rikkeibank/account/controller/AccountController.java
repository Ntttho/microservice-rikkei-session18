package com.rikkeibank.account.controller;

import com.rikkeibank.account.dto.*;
import com.rikkeibank.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Account created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAllAccounts() {
        return ResponseEntity.ok(ApiResponse.success(accountService.getAllAccounts()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(accountService.getAccountById(id)));
    }

    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountByNumber(@PathVariable String accountNumber) {
        return ResponseEntity.ok(ApiResponse.success(accountService.getAccountByNumber(accountNumber)));
    }

    @GetMapping("/my-accounts")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getMyAccounts(
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestParam(value = "email", required = false) String paramEmail) {
        String email = userEmail != null && !userEmail.isBlank() ? userEmail : paramEmail;
        return ResponseEntity.ok(ApiResponse.success(accountService.getAccountsByCustomerEmail(email)));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAccountsByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(ApiResponse.success(accountService.getAccountsByCustomerId(customerId)));
    }

    @PostMapping("/{accountNumber}/deposit")
    public ResponseEntity<ApiResponse<AccountResponse>> deposit(
            @PathVariable String accountNumber,
            @Valid @RequestBody BalanceUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Deposit successful", accountService.deposit(accountNumber, request)));
    }

    @PostMapping("/{accountNumber}/withdraw")
    public ResponseEntity<ApiResponse<AccountResponse>> withdraw(
            @PathVariable String accountNumber,
            @Valid @RequestBody BalanceUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Withdrawal successful", accountService.withdraw(accountNumber, request)));
    }

    // ================= SAGA INTERNAL ENDPOINTS =================

    @PostMapping("/internal/debit")
    public ResponseEntity<ApiResponse<AccountResponse>> debitForTransfer(@Valid @RequestBody DebitCreditRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Debit successful", accountService.debitForTransfer(request)));
    }

    @PostMapping("/internal/credit")
    public ResponseEntity<ApiResponse<AccountResponse>> creditForTransfer(@Valid @RequestBody DebitCreditRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Credit successful", accountService.creditForTransfer(request)));
    }

    @PostMapping("/internal/compensate")
    public ResponseEntity<ApiResponse<AccountResponse>> compensateDebit(@Valid @RequestBody DebitCreditRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Compensation successful", accountService.compensateDebit(request)));
    }
}
