package com.recyclix.backend.dto.collection;

import com.recyclix.backend.model.Collection.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionSummaryDTO {

    private Long id;

    private Long requestId;
    private Long collectorId;

    private BigDecimal realQuantity;
    private BigDecimal totalAmount;

    private PaymentStatus paymentStatus;

    private LocalDateTime collectedAt;
    private LocalDateTime createdAt;

    private BigDecimal rating;
    private String feedbackComment;
}