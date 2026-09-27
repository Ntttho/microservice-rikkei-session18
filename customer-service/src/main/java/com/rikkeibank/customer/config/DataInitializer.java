package com.rikkeibank.customer.config;

import com.rikkeibank.customer.entity.AccountType;
import com.rikkeibank.customer.entity.Customer;
import com.rikkeibank.customer.entity.Staff;
import com.rikkeibank.customer.repository.AccountTypeRepository;
import com.rikkeibank.customer.repository.CustomerRepository;
import com.rikkeibank.customer.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final StaffRepository staffRepository;
    private final AccountTypeRepository accountTypeRepository;

    @Override
    public void run(String... args) {
        if (accountTypeRepository.count() == 0) {
            log.info("Seeding Account Types...");
            accountTypeRepository.save(AccountType.builder()
                    .code("CHECKING")
                    .name("Standard Checking Account")
                    .description("Daily payment and transactional account with zero fees")
                    .interestRate(BigDecimal.valueOf(0.001))
                    .minimumBalance(BigDecimal.valueOf(50000))
                    .dailyTransferLimit(BigDecimal.valueOf(500000000))
                    .active(true)
                    .build());

            accountTypeRepository.save(AccountType.builder()
                    .code("SAVINGS")
                    .name("High-Yield Online Savings")
                    .description("Accumulative high-interest savings account")
                    .interestRate(BigDecimal.valueOf(0.065))
                    .minimumBalance(BigDecimal.valueOf(1000000))
                    .dailyTransferLimit(BigDecimal.valueOf(1000000000))
                    .active(true)
                    .build());

            accountTypeRepository.save(AccountType.builder()
                    .code("VIP")
                    .name("Rikkei Diamond VIP Account")
                    .description("Exclusive account with unlimited daily transactions and priority lounge")
                    .interestRate(BigDecimal.valueOf(0.075))
                    .minimumBalance(BigDecimal.valueOf(50000000))
                    .dailyTransferLimit(BigDecimal.valueOf(5000000000L))
                    .active(true)
                    .build());
        }

        if (customerRepository.count() == 0) {
            log.info("Seeding Customers...");
            customerRepository.save(Customer.builder()
                    .customerCode("CUST-0001")
                    .fullName("Nguyen Van A")
                    .email("customer1@rikkeibank.com")
                    .phoneNumber("0966666666")
                    .identityCardNumber("001200001234")
                    .address("Cau Giay, Hanoi")
                    .dateOfBirth(LocalDate.of(1995, 5, 20))
                    .status("ACTIVE")
                    .build());

            customerRepository.save(Customer.builder()
                    .customerCode("CUST-0002")
                    .fullName("Tran Thi B")
                    .email("customer2@rikkeibank.com")
                    .phoneNumber("0955555555")
                    .identityCardNumber("001200005678")
                    .address("District 1, Ho Chi Minh City")
                    .dateOfBirth(LocalDate.of(1998, 10, 15))
                    .status("ACTIVE")
                    .build());
        }

        if (staffRepository.count() == 0) {
            log.info("Seeding Staff members...");
            staffRepository.save(Staff.builder()
                    .staffCode("STF-001")
                    .fullName("RikkeiBank Teller Staff")
                    .email("teller@rikkeibank.com")
                    .phoneNumber("0977777777")
                    .department("TELLER")
                    .branchCode("HN_MAIN_01")
                    .status("ACTIVE")
                    .build());
        }
    }
}
