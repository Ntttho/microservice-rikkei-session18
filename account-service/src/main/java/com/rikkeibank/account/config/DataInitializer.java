package com.rikkeibank.account.config;

import com.rikkeibank.account.entity.Account;
import com.rikkeibank.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final AccountRepository accountRepository;

    @Override
    public void run(String... args) {
        if (accountRepository.count() == 0) {
            log.info("Seeding initial Bank Accounts...");

            // Customer 1's Checking Account (Nguyen Van A)
            accountRepository.save(Account.builder()
                    .accountNumber("1000188888")
                    .customerId(1L)
                    .customerEmail("customer1@rikkeibank.com")
                    .accountTypeCode("CHECKING")
                    .balance(BigDecimal.valueOf(15000000)) // 15,000,000 VND
                    .currency("VND")
                    .status("ACTIVE")
                    .build());

            // Customer 1's Savings Account
            accountRepository.save(Account.builder()
                    .accountNumber("1000199999")
                    .customerId(1L)
                    .customerEmail("customer1@rikkeibank.com")
                    .accountTypeCode("SAVINGS")
                    .balance(BigDecimal.valueOf(50000000)) // 50,000,000 VND
                    .currency("VND")
                    .status("ACTIVE")
                    .build());

            // Customer 2's Checking Account (Tran Thi B)
            accountRepository.save(Account.builder()
                    .accountNumber("1000288888")
                    .customerId(2L)
                    .customerEmail("customer2@rikkeibank.com")
                    .accountTypeCode("CHECKING")
                    .balance(BigDecimal.valueOf(5000000)) // 5,000,000 VND
                    .currency("VND")
                    .status("ACTIVE")
                    .build());

            log.info("Seeded 3 bank accounts: 1000188888 (15M), 1000199999 (50M), 1000288888 (5M)");
        }
    }
}
