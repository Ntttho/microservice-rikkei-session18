package com.rikkeibank.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse implements Serializable {
    private Long id;
    private String customerCode;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String identityCardNumber;
    private String address;
    private LocalDate dateOfBirth;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
