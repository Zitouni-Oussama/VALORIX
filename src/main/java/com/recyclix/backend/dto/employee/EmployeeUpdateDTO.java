package com.recyclix.backend.dto.employee;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeUpdateDTO {

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Past(message = "La date de naissance doit être dans le passé.")
    private LocalDate birthDate;

    @Pattern(
            regexp = "^(05|06|07)[0-9]{8}$",
            message = "Le téléphone doit commencer par 05, 06 ou 07 et contenir 10 chiffres."
    )
    private String phone;

    @DecimalMin(value = "0.0", inclusive = false, message = "Le salaire doit être supérieur à zéro.")
    private BigDecimal salaryAmount;

    private Boolean active;

    private String wilaya;
    private Long recyclingCenterId;
}