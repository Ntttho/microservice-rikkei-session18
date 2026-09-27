package com.rikkeibank.customer.service;

import com.rikkeibank.customer.dto.AccountTypeDto;
import com.rikkeibank.customer.entity.AccountType;
import com.rikkeibank.customer.exception.BadRequestException;
import com.rikkeibank.customer.exception.ResourceNotFoundException;
import com.rikkeibank.customer.repository.AccountTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountTypeService {

    private final AccountTypeRepository accountTypeRepository;

    @Transactional
    @CachePut(value = "accountTypes", key = "#result.id")
    public AccountTypeDto createAccountType(AccountTypeDto dto) {
        if (accountTypeRepository.existsByCode(dto.getCode())) {
            throw new BadRequestException("Account type code " + dto.getCode() + " already exists");
        }

        AccountType entity = AccountType.builder()
                .code(dto.getCode().toUpperCase())
                .name(dto.getName())
                .description(dto.getDescription())
                .interestRate(dto.getInterestRate())
                .minimumBalance(dto.getMinimumBalance())
                .dailyTransferLimit(dto.getDailyTransferLimit())
                .active(dto.isActive())
                .build();

        AccountType saved = accountTypeRepository.save(entity);
        log.info("Created Account Type: {} ({})", saved.getName(), saved.getCode());
        return mapToDto(saved);
    }

    @Cacheable(value = "accountTypes", key = "#id")
    public AccountTypeDto getAccountTypeById(Long id) {
        AccountType entity = accountTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account type not found with ID: " + id));
        return mapToDto(entity);
    }

    @Cacheable(value = "accountTypes", key = "'code_' + #code")
    public AccountTypeDto getAccountTypeByCode(String code) {
        AccountType entity = accountTypeRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Account type not found with code: " + code));
        return mapToDto(entity);
    }

    public List<AccountTypeDto> getAllAccountTypes() {
        return accountTypeRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @CachePut(value = "accountTypes", key = "#id")
    public AccountTypeDto updateAccountType(Long id, AccountTypeDto dto) {
        AccountType entity = accountTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account type not found with ID: " + id));

        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setInterestRate(dto.getInterestRate());
        entity.setMinimumBalance(dto.getMinimumBalance());
        entity.setDailyTransferLimit(dto.getDailyTransferLimit());
        entity.setActive(dto.isActive());

        AccountType updated = accountTypeRepository.save(entity);
        log.info("Updated Account Type ID: {}", id);
        return mapToDto(updated);
    }

    @Transactional
    @CacheEvict(value = "accountTypes", key = "#id")
    public void deleteAccountType(Long id) {
        if (!accountTypeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Account type not found with ID: " + id);
        }
        accountTypeRepository.deleteById(id);
        log.info("Deleted Account Type ID: {}", id);
    }

    private AccountTypeDto mapToDto(AccountType entity) {
        return AccountTypeDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .interestRate(entity.getInterestRate())
                .minimumBalance(entity.getMinimumBalance())
                .dailyTransferLimit(entity.getDailyTransferLimit())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
