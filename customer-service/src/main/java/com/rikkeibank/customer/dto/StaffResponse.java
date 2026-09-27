package com.rikkeibank.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffResponse implements Serializable {
    private Long id;
    private String staffCode;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String department;
    private String branchCode;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
