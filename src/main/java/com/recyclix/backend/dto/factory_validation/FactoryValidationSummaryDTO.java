package com.recyclix.backend.dto.factory_validation;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FactoryValidationSummaryDTO {

    private Long id;

    private Long deliveryId;
    private Long validatedById;

    private BigDecimal validatedWeight;

    private LocalDateTime validatedAt;
}