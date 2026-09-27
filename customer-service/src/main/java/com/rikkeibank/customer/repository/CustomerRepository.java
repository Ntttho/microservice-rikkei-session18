package com.rikkeibank.customer.repository;

import com.rikkeibank.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByCustomerCode(String customerCode);
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByIdentityCardNumber(String identityCardNumber);
    boolean existsByEmail(String email);
    boolean existsByCustomerCode(String customerCode);
    boolean existsByIdentityCardNumber(String identityCardNumber);
}
