package com.recyclix.backend.dto.payroll;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeductionResponseDTO {

    private Long id;

    private Long employeeId;
    private String employeeFullName;

    private BigDecimal amount;
    private LocalDate deductionDate;
    private String reason;

    private Long recordedById;
    private String recordedByFullName;

    private LocalDateTime createdAt;
}