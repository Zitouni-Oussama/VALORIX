package com.recyclix.backend.dto.ai_classification;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIClassificationUpdateDTO {

    // ⚠️ requestId NON modifiable ici (OneToOne unique obligatoire)
    private Long predictedMaterialId;

    private BigDecimal predictedWeight;

    private BigDecimal confidenceScore;

    @Size(max = 50, message = "La version du modèle IA ne peut pas dépasser 50 caractères.")
    private String aiModelVersion;

    private Boolean isValidated;

    // si on valide manuellement
    private Long validatedById;
}