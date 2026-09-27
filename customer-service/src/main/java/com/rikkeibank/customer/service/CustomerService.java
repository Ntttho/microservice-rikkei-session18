package com.rikkeibank.customer.service;

import com.rikkeibank.customer.dto.CustomerRequest;
import com.rikkeibank.customer.dto.CustomerResponse;
import com.rikkeibank.customer.entity.Customer;
import com.rikkeibank.customer.exception.BadRequestException;
import com.rikkeibank.customer.exception.ResourceNotFoundException;
import com.rikkeibank.customer.repository.CustomerRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
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
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional
    @CachePut(value = "customers", key = "#result.id")
    public CustomerResponse createCustomer(CustomerRequest request) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Customer with email " + request.getEmail() + " already exists");
        }
        if (customerRepository.existsByCustomerCode(request.getCustomerCode())) {
            throw new BadRequestException("Customer code " + request.getCustomerCode() + " already exists");
        }
        if (customerRepository.existsByIdentityCardNumber(request.getIdentityCardNumber())) {
            throw new BadRequestException("Identity card " + request.getIdentityCardNumber() + " already registered");
        }

        Customer customer = Customer.builder()
                .customerCode(request.getCustomerCode())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .identityCardNumber(request.getIdentityCardNumber())
                .address(request.getAddress())
                .dateOfBirth(request.getDateOfBirth())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .build();

        Customer saved = customerRepository.save(customer);
        log.info("Created customer: {} (Code: {})", saved.getFullName(), saved.getCustomerCode());
        return mapToResponse(saved);
    }

    @Cacheable(value = "customers", key = "#id")
    @CircuitBreaker(name = "customerCB", fallbackMethod = "getCustomerByIdFallback")
    public CustomerResponse getCustomerById(Long id) {
        log.info("Fetching customer from database for ID: {}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));
        return mapToResponse(customer);
    }

    public CustomerResponse getCustomerByIdFallback(Long id, Throwable t) {
        log.warn("Fallback triggered for getCustomerById({}): {}", id, t.getMessage());
        return CustomerResponse.builder()
                .id(id)
                .customerCode("CUST-FALLBACK")
                .fullName("System Temporary (Service Degradation)")
                .email("fallback@rikkeibank.com")
                .phoneNumber("N/A")
                .status("UNKNOWN")
                .build();
    }

    @Cacheable(value = "customers", key = "'code_' + #customerCode")
    public CustomerResponse getCustomerByCode(String customerCode) {
        Customer customer = customerRepository.findByCustomerCode(customerCode)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with code: " + customerCode));
        return mapToResponse(customer);
    }

    public CustomerResponse getCustomerByEmail(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with email: " + email));
        return mapToResponse(customer);
    }

    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @CachePut(value = "customers", key = "#id")
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));

        customer.setFullName(request.getFullName());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setAddress(request.getAddress());
        customer.setDateOfBirth(request.getDateOfBirth());
        if (request.getStatus() != null) {
            customer.setStatus(request.getStatus());
        }

        Customer updated = customerRepository.save(customer);
        log.info("Updated customer: {} (ID: {})", updated.getFullName(), updated.getId());
        return mapToResponse(updated);
    }

    @Transactional
    @CacheEvict(value = "customers", key = "#id")
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer not found with ID: " + id);
        }
        customerRepository.deleteById(id);
        log.info("Deleted customer with ID: {}", id);
    }

    private CustomerResponse mapToResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .customerCode(customer.getCustomerCode())
                .fullName(customer.getFullName())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .identityCardNumber(customer.getIdentityCardNumber())
                .address(customer.getAddress())
                .dateOfBirth(customer.getDateOfBirth())
                .status(customer.getStatus())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}
