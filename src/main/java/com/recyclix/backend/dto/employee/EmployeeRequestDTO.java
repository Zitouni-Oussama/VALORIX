package com.recyclix.backend.dto.employee;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeRequestDTO {

    @NotBlank(message = "Le prénom est obligatoire.")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire.")
    @Size(max = 100)
    private String lastName;

    @Past(message = "La date de naissance doit être dans le passé.")
    private LocalDate birthDate;

    @Pattern(
            regexp = "^(05|06|07)[0-9]{8}$",
            message = "Le téléphone doit commencer par 05, 06 ou 07 et contenir 10 chiffres."
    )
    private String phone;

    @NotNull(message = "Le salaire est obligatoire.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le salaire doit être supérieur à zéro.")
    private BigDecimal salaryAmount;

    private String wilaya;
    private Long recyclingCenterId; // optionnel en création (sera forcé par le service)
}