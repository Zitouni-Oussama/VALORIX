package com.recyclix.backend.dto.collection_request;

import com.recyclix.backend.model.CollectionRequest.Status;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionRequestUpdateDTO {

    // ⚠️ clientId non modifiable ici (relation obligatoire / structurelle)
    private Long materialId;

    private BigDecimal estimatedQuantity;
    private BigDecimal estimatedAmount;

    @Size(max = 255, message = "L'URL de l'image des déchets ne peut pas dépasser 255 caractères.")
    private String wasteImageUrl;

    private BigDecimal latitude;
    private BigDecimal longitude;

    private BigDecimal collectorPrice;

    private Status status;
}