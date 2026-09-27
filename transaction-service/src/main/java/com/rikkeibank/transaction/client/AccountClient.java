package com.rikkeibank.transaction.client;

import com.rikkeibank.transaction.dto.AccountDto;
import com.rikkeibank.transaction.dto.ApiResponse;
import com.rikkeibank.transaction.dto.DebitCreditRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ACCOUNT-SERVICE", fallback = AccountClientFallback.class)
public interface AccountClient {

    @GetMapping("/api/v1/accounts/number/{accountNumber}")
    ApiResponse<AccountDto> getAccountByNumber(@PathVariable("accountNumber") String accountNumber);

    @PostMapping("/api/v1/accounts/internal/debit")
    ApiResponse<AccountDto> debitForTransfer(@RequestBody DebitCreditRequest request);

    @PostMapping("/api/v1/accounts/internal/credit")
    ApiResponse<AccountDto> creditForTransfer(@RequestBody DebitCreditRequest request);

    @PostMapping("/api/v1/accounts/internal/compensate")
    ApiResponse<AccountDto> compensateDebit(@RequestBody DebitCreditRequest request);
}
