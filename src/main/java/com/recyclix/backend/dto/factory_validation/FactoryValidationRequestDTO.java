package com.recyclix.backend.dto.factory_validation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FactoryValidationRequestDTO {

    @NotNull(message = "Le deliveryId ne peut pas être nul.")
    private Long deliveryId;

    @NotNull(message = "Le validatedById ne peut pas être nul.")
    private Long validatedById;

    @NotNull(message = "Le poids déclaré ne peut pas être nul.")
    private BigDecimal declaredWeight;

    @NotNull(message = "Le poids validé ne peut pas être nul.")
    private BigDecimal validatedWeight;

    @Size(max = 500, message = "La note d'ajustement ne peut pas dépasser 500 caractères.")
    private String adjustmentNote;

    @Size(max = 500, message = "La raison de rejet ne peut pas dépasser 500 caractères.")
    private String rejectionReason;
}