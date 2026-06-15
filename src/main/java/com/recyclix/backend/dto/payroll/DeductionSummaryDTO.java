package com.recyclix.backend.dto.payroll;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeductionSummaryDTO {

    private Long id;

    private Long employeeId;
    private String employeeFullName;

    private BigDecimal amount;
    private LocalDate deductionDate;
    private String reason;
}