package com.recyclix.backend.dto.employee;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeSummaryDTO {

    private Long id;

    private String firstName;
    private String lastName;
    private String fullName;

    private String phone;
    private BigDecimal salaryAmount;

    private Boolean active;

    private BigDecimal totalDeductions;
    private BigDecimal netSalary;

    private String wilaya;
    private String recyclingCenterName;
}