package com.rikkeibank.customer.service;

import com.rikkeibank.customer.dto.StaffRequest;
import com.rikkeibank.customer.dto.StaffResponse;
import com.rikkeibank.customer.entity.Staff;
import com.rikkeibank.customer.exception.BadRequestException;
import com.rikkeibank.customer.exception.ResourceNotFoundException;
import com.rikkeibank.customer.repository.StaffRepository;
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
public class StaffService {

    private final StaffRepository staffRepository;

    @Transactional
    @CachePut(value = "staffs", key = "#result.id")
    public StaffResponse createStaff(StaffRequest request) {
        if (staffRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Staff with email " + request.getEmail() + " already exists");
        }
        if (staffRepository.existsByStaffCode(request.getStaffCode())) {
            throw new BadRequestException("Staff code " + request.getStaffCode() + " already exists");
        }

        Staff staff = Staff.builder()
                .staffCode(request.getStaffCode())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .department(request.getDepartment())
                .branchCode(request.getBranchCode())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .build();

        Staff saved = staffRepository.save(staff);
        log.info("Created staff: {} (Code: {})", saved.getFullName(), saved.getStaffCode());
        return mapToResponse(saved);
    }

    @Cacheable(value = "staffs", key = "#id")
    public StaffResponse getStaffById(Long id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with ID: " + id));
        return mapToResponse(staff);
    }

    public List<StaffResponse> getAllStaffs() {
        return staffRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @CachePut(value = "staffs", key = "#id")
    public StaffResponse updateStaff(Long id, StaffRequest request) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with ID: " + id));

        staff.setFullName(request.getFullName());
        staff.setPhoneNumber(request.getPhoneNumber());
        staff.setDepartment(request.getDepartment());
        staff.setBranchCode(request.getBranchCode());
        if (request.getStatus() != null) {
            staff.setStatus(request.getStatus());
        }

        Staff updated = staffRepository.save(staff);
        log.info("Updated staff: {} (ID: {})", updated.getFullName(), updated.getId());
        return mapToResponse(updated);
    }

    @Transactional
    @CacheEvict(value = "staffs", key = "#id")
    public void deleteStaff(Long id) {
        if (!staffRepository.existsById(id)) {
            throw new ResourceNotFoundException("Staff not found with ID: " + id);
        }
        staffRepository.deleteById(id);
        log.info("Deleted staff with ID: {}", id);
    }

    private StaffResponse mapToResponse(Staff staff) {
        return StaffResponse.builder()
                .id(staff.getId())
                .staffCode(staff.getStaffCode())
                .fullName(staff.getFullName())
                .email(staff.getEmail())
                .phoneNumber(staff.getPhoneNumber())
                .department(staff.getDepartment())
                .branchCode(staff.getBranchCode())
                .status(staff.getStatus())
                .createdAt(staff.getCreatedAt())
                .updatedAt(staff.getUpdatedAt())
                .build();
    }
}
