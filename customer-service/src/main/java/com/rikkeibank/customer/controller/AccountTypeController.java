package com.rikkeibank.customer.controller;

import com.rikkeibank.customer.dto.AccountTypeDto;
import com.rikkeibank.customer.dto.ApiResponse;
import com.rikkeibank.customer.service.AccountTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/account-types")
@RequiredArgsConstructor
public class AccountTypeController {

    private final AccountTypeService accountTypeService;

    @PostMapping
    public ResponseEntity<ApiResponse<AccountTypeDto>> createAccountType(@Valid @RequestBody AccountTypeDto request) {
        AccountTypeDto response = accountTypeService.createAccountType(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Account type created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountTypeDto>>> getAllAccountTypes() {
        return ResponseEntity.ok(ApiResponse.success(accountTypeService.getAllAccountTypes()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountTypeDto>> getAccountTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(accountTypeService.getAccountTypeById(id)));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<AccountTypeDto>> getAccountTypeByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success(accountTypeService.getAccountTypeByCode(code)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountTypeDto>> updateAccountType(
            @PathVariable Long id,
            @Valid @RequestBody AccountTypeDto request) {
        return ResponseEntity.ok(ApiResponse.success("Account type updated successfully", accountTypeService.updateAccountType(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAccountType(@PathVariable Long id) {
        accountTypeService.deleteAccountType(id);
        return ResponseEntity.ok(ApiResponse.success("Account type deleted successfully", null));
    }
}
