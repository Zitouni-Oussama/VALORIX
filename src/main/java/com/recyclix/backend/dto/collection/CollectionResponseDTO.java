package com.recyclix.backend.dto.collection;

import com.recyclix.backend.model.Collection.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionResponseDTO {

    private Long id;

    private Long requestId;
    private Long collectorId;

    private BigDecimal realQuantity;
    private BigDecimal unitPriceFrozen;
    private BigDecimal totalAmount;

    private String collectionProofImageUrl;

    private PaymentStatus paymentStatus;

    private LocalDateTime collectedAt;
    private LocalDateTime createdAt;

    private Long factoryDeliveryId;
    private Long transactionId;
    private Long pointMovementId;

    private BigDecimal rating;
    private String feedbackComment;
    private LocalDateTime ratedAt;
}