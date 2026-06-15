package com.recyclix.backend.dto.employee;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponseDTO {

    private Long id;

    private String firstName;
    private String lastName;
    private String fullName;

    private LocalDate birthDate;
    private String phone;
    private BigDecimal salaryAmount;

    private Boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String wilaya;
    private Long recyclingCenterId;
    private String recyclingCenterName;
}