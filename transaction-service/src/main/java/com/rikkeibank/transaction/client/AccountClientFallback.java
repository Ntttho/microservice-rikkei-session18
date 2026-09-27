package com.rikkeibank.transaction.client;

import com.rikkeibank.transaction.dto.AccountDto;
import com.rikkeibank.transaction.dto.ApiResponse;
import com.rikkeibank.transaction.dto.DebitCreditRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AccountClientFallback implements AccountClient {

    @Override
    public ApiResponse<AccountDto> getAccountByNumber(String accountNumber) {
        log.error("AccountClient fallback triggered for getAccountByNumber: {}", accountNumber);
        return ApiResponse.error(503, "Account Service is currently unavailable");
    }

    @Override
    public ApiResponse<AccountDto> debitForTransfer(DebitCreditRequest request) {
        log.error("AccountClient fallback triggered for debitForTransfer: {}", request.getAccountNumber());
        return ApiResponse.error(503, "Account Service unavailable during debit");
    }

    @Override
    public ApiResponse<AccountDto> creditForTransfer(DebitCreditRequest request) {
        log.error("AccountClient fallback triggered for creditForTransfer: {}", request.getAccountNumber());
        return ApiResponse.error(503, "Account Service unavailable during credit");
    }

    @Override
    public ApiResponse<AccountDto> compensateDebit(DebitCreditRequest request) {
        log.error("AccountClient fallback triggered for compensateDebit: {}", request.getAccountNumber());
        return ApiResponse.error(503, "Account Service unavailable during compensation");
    }
}
