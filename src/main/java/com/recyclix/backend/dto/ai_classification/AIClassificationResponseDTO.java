package com.recyclix.backend.dto.ai_classification;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIClassificationResponseDTO {

    private Long id;

    private Long requestId;

    private Long predictedMaterialId;
    private BigDecimal predictedWeight;
    private BigDecimal confidenceScore;

    private String aiModelVersion;

    private Boolean isValidated;
    private LocalDateTime createdAt;

    private Long validatedById;
}