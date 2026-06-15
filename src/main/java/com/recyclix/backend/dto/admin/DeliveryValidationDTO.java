package com.recyclix.backend.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryValidationDTO {
    private Long deliveryId;
    private Long collectionId;
    private Long clientId;
    private String clientName;
    private Long collectorId;
    private String collectorName;
    private String materialName;
    private BigDecimal declaredWeight;
    private BigDecimal validatedWeight;
    private String status;               // PENDING, VALIDATED, ADJUSTED, REFUSED, PROCESSING, COMPLETED
    private String adjustmentNote;
    private String rejectionReason;
    private String validatedByName;
    private LocalDateTime validatedAt;
    private String wastePhoto;

    private String recyclingCenterName;
}