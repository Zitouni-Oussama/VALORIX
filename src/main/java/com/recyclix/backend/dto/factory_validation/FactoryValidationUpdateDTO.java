package com.recyclix.backend.dto.factory_validation;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FactoryValidationUpdateDTO {

    // ⚠️ deliveryId et validatedById NON modifiables (setter NONE dans l'entity)

    private BigDecimal declaredWeight;
    private BigDecimal validatedWeight;

    @Size(max = 500, message = "La note d'ajustement ne peut pas dépasser 500 caractères.")
    private String adjustmentNote;

    @Size(max = 500, message = "La raison de rejet ne peut pas dépasser 500 caractères.")
    private String rejectionReason;
}