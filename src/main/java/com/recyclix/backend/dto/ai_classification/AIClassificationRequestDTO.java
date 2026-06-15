package com.recyclix.backend.dto.ai_classification;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIClassificationRequestDTO {

    @NotNull(message = "Le requestId ne peut pas être nul.")
    private Long requestId;

    @NotNull(message = "Le predictedMaterialId ne peut pas être nul.")
    private Long predictedMaterialId;

    @NotNull(message = "Le predictedWeight ne peut pas être nul.")
    private BigDecimal predictedWeight;

    @NotNull(message = "Le confidenceScore ne peut pas être nul.")
    private BigDecimal confidenceScore;

    @Size(max = 50, message = "La version du modèle IA ne peut pas dépasser 50 caractères.")
    private String aiModelVersion;

    // optionnel : si null -> @PrePersist met false
    private Boolean isValidated;

    // optionnel : validateur (FactoryUser)
    private Long validatedById;
}