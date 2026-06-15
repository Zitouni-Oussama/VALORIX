// recyclix\backend\dto\admin\RequestFullDetailDTO.java
package com.recyclix.backend.dto.admin;

import com.recyclix.backend.model.CollectionRequest.Status;
import com.recyclix.backend.model.Collection.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestFullDetailDTO {

    // Requête
    private Long requestId;
    private Status requestStatus;
    private BigDecimal estimatedQuantity;
    private BigDecimal estimatedAmount;
    private BigDecimal collectorPrice;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String wasteImageUrl;
    private LocalDateTime requestCreatedAt;
    private LocalDateTime requestUpdatedAt;

    // Client
    private Long clientId;
    private String clientFullName;
    private String clientEmail;
    private String clientPhone;
    private String clientAddress;

    // Matériau
    private Long materialId;
    private String materialName;

    // Classification IA
    private Long aiClassificationId;
    private Long predictedMaterialId;
    private BigDecimal predictedWeight;
    private BigDecimal confidenceScore;
    private Boolean aiValidated;

    // Collecteur
    private Long collectorId;
    private String collectorFullName;
    private String collectorEmail;
    private String collectorPhone;

    // Collecte
    private Long collectionId;
    private BigDecimal realQuantity;
    private BigDecimal unitPriceFrozen;
    private BigDecimal totalAmount;
    private PaymentStatus paymentStatus;
    private String collectionProofImageUrl;
    private LocalDateTime collectedAt;

    // Livraison usine
    private Long deliveryId;
    private String deliveryStatus;
    private LocalDateTime deliveryDate;

    // Validation usine
    private Long validationId;
    private BigDecimal declaredWeight;
    private BigDecimal validatedWeight;
    private String adjustmentNote;
    private String rejectionReason;
    private Long validatedById;
    private String validatedByFullName;
    private LocalDateTime validatedAt;

    // Litiges
    private boolean hasOpenTicket;
    private Long relatedTicketId;
    private String ticketSubject;
}