package com.recyclix.backend.dto.payroll;

import jakarta.validation.constraints.DecimalMin;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeductionUpdateDTO {

    private Long employeeId;

    @DecimalMin(value = "0.0", inclusive = false, message = "Le montant doit être supérieur à zéro.")
    private BigDecimal amount;

    private LocalDate deductionDate;

    private String reason;
}