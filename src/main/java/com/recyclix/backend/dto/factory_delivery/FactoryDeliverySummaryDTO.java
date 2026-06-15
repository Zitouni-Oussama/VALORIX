package com.recyclix.backend.dto.factory_delivery;

import com.recyclix.backend.model.FactoryDelivery.DeliveryStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FactoryDeliverySummaryDTO {

    private Long id;

    private Long collectionId;

    private DeliveryStatus status;
    private LocalDateTime deliveryDate;

    private LocalDateTime createdAt;
}