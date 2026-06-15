package com.recyclix.backend.dto.factory_delivery;

import com.recyclix.backend.model.FactoryDelivery.DeliveryStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FactoryDeliveryResponseDTO {

    private Long id;

    private DeliveryStatus status;
    private LocalDateTime deliveryDate;
    private LocalDateTime createdAt;

    private Long collectionId;

    private Long validationId;

    private Long recyclingCenterId;
}