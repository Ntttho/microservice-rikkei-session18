package com.rikkeibank.account.service;

import com.rikkeibank.account.dto.AccountResponse;
import com.rikkeibank.account.dto.BalanceUpdateRequest;
import com.rikkeibank.account.dto.CreateAccountRequest;
import com.rikkeibank.account.dto.DebitCreditRequest;
import com.rikkeibank.account.entity.Account;
import com.rikkeibank.account.exception.BadRequestException;
import com.rikkeibank.account.exception.InsufficientBalanceException;
import com.rikkeibank.account.exception.ResourceNotFoundException;
import com.rikkeibank.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        String generatedAccountNumber;
        do {
            generatedAccountNumber = "100" + String.format("%07d", random.nextInt(10000000));
        } while (accountRepository.existsByAccountNumber(generatedAccountNumber));

        BigDecimal initialBalance = request.getInitialDeposit() != null ? request.getInitialDeposit() : BigDecimal.ZERO;

        Account account = Account.builder()
                .accountNumber(generatedAccountNumber)
                .customerId(request.getCustomerId())
                .customerEmail(request.getCustomerEmail())
                .accountTypeCode(request.getAccountTypeCode().toUpperCase())
                .balance(initialBalance)
                .currency(request.getCurrency() != null ? request.getCurrency() : "VND")
                .status("ACTIVE")
                .build();

        Account saved = accountRepository.save(account);
        log.info("Created new Account: {} for customer: {}", saved.getAccountNumber(), saved.getCustomerEmail());
        return mapToResponse(saved);
    }

    @Cacheable(value = "accounts", key = "'acc_num_' + #accountNumber")
    public AccountResponse getAccountByNumber(String accountNumber) {
        log.info("Fetching account from DB for account number: {}", accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with number: " + accountNumber));
        return mapToResponse(account);
    }

    @Cacheable(value = "accounts", key = "#id")
    public AccountResponse getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + id));
        return mapToResponse(account);
    }

    public List<AccountResponse> getAccountsByCustomerId(Long customerId) {
        return accountRepository.findByCustomerId(customerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<AccountResponse> getAccountsByCustomerEmail(String customerEmail) {
        return accountRepository.findByCustomerEmail(customerEmail).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "accounts", allEntries = true)
    public AccountResponse deposit(String accountNumber, BalanceUpdateRequest request) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));

        if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) {
            throw new BadRequestException("Account is not active");
        }

        account.setBalance(account.getBalance().add(request.getAmount()));
        Account saved = accountRepository.save(account);
        log.info("Deposited {} to account {}. New balance: {}", request.getAmount(), accountNumber, saved.getBalance());
        return mapToResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "accounts", allEntries = true)
    public AccountResponse withdraw(String accountNumber, BalanceUpdateRequest request) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));

        if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) {
            throw new BadRequestException("Account is not active");
        }

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient funds. Current balance: " + account.getBalance() + ", Requested: " + request.getAmount());
        }

        account.setBalance(account.getBalance().subtract(request.getAmount()));
        Account saved = accountRepository.save(account);
        log.info("Withdrawn {} from account {}. Remaining balance: {}", request.getAmount(), accountNumber, saved.getBalance());
        return mapToResponse(saved);
    }

    // ================= SAGA PATTERN PARTICIPANT METHODS =================

    @Transactional
    @CacheEvict(value = "accounts", allEntries = true)
    public AccountResponse debitForTransfer(DebitCreditRequest request) {
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Sender account not found: " + request.getAccountNumber()));

        if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) {
            throw new BadRequestException("Sender account is locked or inactive: " + request.getAccountNumber());
        }

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Sender has insufficient balance: Current=" + account.getBalance() + ", Transfer=" + request.getAmount());
        }

        account.setBalance(account.getBalance().subtract(request.getAmount()));
        Account saved = accountRepository.save(account);
        log.info("[Saga Step 1] Successfully debited {} from sender account {}. Remaining: {}", request.getAmount(), request.getAccountNumber(), saved.getBalance());
        return mapToResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "accounts", allEntries = true)
    public AccountResponse creditForTransfer(DebitCreditRequest request) {
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver account not found: " + request.getAccountNumber()));

        if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) {
            throw new BadRequestException("Receiver account is inactive: " + request.getAccountNumber());
        }

        account.setBalance(account.getBalance().add(request.getAmount()));
        Account saved = accountRepository.save(account);
        log.info("[Saga Step 2] Successfully credited {} to receiver account {}. New balance: {}", request.getAmount(), request.getAccountNumber(), saved.getBalance());
        return mapToResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "accounts", allEntries = true)
    public AccountResponse compensateDebit(DebitCreditRequest request) {
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Sender account not found for compensation: " + request.getAccountNumber()));

        account.setBalance(account.getBalance().add(request.getAmount()));
        Account saved = accountRepository.save(account);
        log.warn("[Saga Compensating] Successfully refunded {} to sender account {}. Restored balance: {}", request.getAmount(), request.getAccountNumber(), saved.getBalance());
        return mapToResponse(saved);
    }

    private AccountResponse mapToResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .customerId(account.getCustomerId())
                .customerEmail(account.getCustomerEmail())
                .accountTypeCode(account.getAccountTypeCode())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
