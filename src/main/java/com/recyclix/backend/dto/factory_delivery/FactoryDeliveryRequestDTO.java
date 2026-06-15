package com.recyclix.backend.dto.factory_delivery;

import com.recyclix.backend.model.FactoryDelivery.DeliveryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FactoryDeliveryRequestDTO {

    @NotNull(message = "Le collectionId ne peut pas être nul.")
    private Long collectionId;

    // optionnel : si null -> @PrePersist met PENDING
    private DeliveryStatus status;

    // optionnel
    private LocalDateTime deliveryDate;

    // optionnel
    private Long recyclingCenterId;
}