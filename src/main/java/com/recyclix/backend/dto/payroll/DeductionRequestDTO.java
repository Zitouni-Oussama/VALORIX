package com.recyclix.backend.dto.payroll;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DeductionRequestDTO {

    @NotNull(message = "L'employé est obligatoire.")
    private Long employeeId;

    @NotNull(message = "Le montant est requis.")
    @Positive(message = "Le montant doit être supérieur à zéro.")
    private BigDecimal amount;

    @NotNull(message = "La date est requise.")
    private LocalDate deductionDate;

    private String reason;
}