// dto/workshop/CollectionFullDetailsDTO.java
package com.recyclix.backend.dto.workshop;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CollectionFullDetailsDTO {

    // === Infos collecte ===
    private Long collectionId;
    private BigDecimal realQuantity;
    private BigDecimal totalAmount;
    private String collectionProofImageUrl;
    private LocalDateTime collectedAt;
    private String paymentStatus;       // PAID / UNPAID

    // === Infos demande de collecte ===
    private Long requestId;
    private String requestStatus;       // PENDING, ACCEPTED, COLLECTED, etc.
    private BigDecimal estimatedQuantity;
    private BigDecimal estimatedAmount;
    private BigDecimal collectorPrice;
    private String wasteImageUrl;
    private String address;             // adresse du client
    private BigDecimal latitude;
    private BigDecimal longitude;

    // === Infos client ===
    private Long clientId;
    private String clientFirstName;
    private String clientLastName;
    private String clientEmail;
    private String clientPhone;

    // === Infos collecteur ===
    private Long collectorId;
    private String collectorFirstName;
    private String collectorLastName;
    private String collectorEmail;
    private String collectorPhone;
    private Boolean isCollectorVerified;
    private BigDecimal collectorAverageRating;

    // === Infos livraison usine ===
    private Long deliveryId;
    private String deliveryStatus;     // PENDING, VALIDATED, ADJUSTED, REFUSED, PROCESSING, COMPLETED
    private LocalDateTime deliveryDate;
    private LocalDateTime deliveryCreatedAt;

    // === Infos validation usine ===
    private Long validationId;
    private BigDecimal declaredWeight;
    private BigDecimal validatedWeight;
    private String adjustmentNote;
    private String rejectionReason;
    private Long validatedByUserId;
    private LocalDateTime validatedAt;
}