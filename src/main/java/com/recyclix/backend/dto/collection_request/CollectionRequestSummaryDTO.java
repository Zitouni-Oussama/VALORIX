package com.recyclix.backend.dto.collection_request;

import com.recyclix.backend.model.CollectionRequest.Status;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionRequestSummaryDTO {

    private Long id;

    private Long clientId;
    private Long materialId;

    private BigDecimal estimatedQuantity;
    private BigDecimal estimatedAmount;

    private BigDecimal collectorPrice;

    private Status status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}