package com.recyclix.backend.dto.factory_delivery;

import com.recyclix.backend.model.FactoryDelivery.DeliveryStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FactoryDeliveryUpdateDTO {

    // ⚠️ collectionId NON modifiable (OneToOne unique obligatoire)
    private DeliveryStatus status;

    private LocalDateTime deliveryDate;

    private Long recyclingCenterId;
}