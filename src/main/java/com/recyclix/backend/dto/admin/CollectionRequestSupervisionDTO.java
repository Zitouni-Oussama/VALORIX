// recyclix\backend\dto\admin\CollectionRequestSupervisionDTO.java
package com.recyclix.backend.dto.admin;

import com.recyclix.backend.model.CollectionRequest.Status;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionRequestSupervisionDTO {

    private Long requestId;
    private Status status;

    private Long clientId;
    private String clientFullName;
    private String clientEmail;

    private Long materialId;
    private String materialName;

    private BigDecimal estimatedQuantity;
    private BigDecimal estimatedAmount;
    private BigDecimal collectorPrice;

    private Long collectorId;
    private String collectorFullName;

    private Long collectionId;
    private BigDecimal realQuantity;
    private BigDecimal totalAmount;

    private String deliveryStatus;
    private String validationStatus;

    private boolean hasOpenTicket;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}