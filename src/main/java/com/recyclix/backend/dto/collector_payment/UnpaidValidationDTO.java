package com.recyclix.backend.dto.collector_payment;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnpaidValidationDTO {
    private Long validationId;
    private Long deliveryId;
    private Long collectionId;
    private LocalDateTime validatedAt;
    private BigDecimal validatedWeight;
    private BigDecimal collectorAmount;
    private String materialName;
}