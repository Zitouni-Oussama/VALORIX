package com.recyclix.backend.dto.factory_validation;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FactoryValidationResponseDTO {

    private Long id;

    private BigDecimal declaredWeight;
    private BigDecimal validatedWeight;

    private String adjustmentNote;
    private String rejectionReason;

    private LocalDateTime validatedAt;

    private Long deliveryId;
    private Long validatedById;
}