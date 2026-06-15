package com.recyclix.backend.dto.ai_classification;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIClassificationSummaryDTO {

    private Long id;
    private Long requestId;

    private Long predictedMaterialId;
    private BigDecimal confidenceScore;

    private Boolean isValidated;
    private LocalDateTime createdAt;
}