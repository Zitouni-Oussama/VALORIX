package com.recyclix.backend.dto.collection_request;

import com.recyclix.backend.model.CollectionRequest.Status;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionRequestRequestDTO {

    // optionnel
    private Long materialId;

    @NotNull(message = "La quantité estimée ne peut pas être nulle.")
    private BigDecimal estimatedQuantity;

    @NotNull(message = "Le montant estimé ne peut pas être nul.")
    private BigDecimal estimatedAmount;

    @Size(max = 255, message = "L'URL de l'image des déchets ne peut pas dépasser 255 caractères.")
    private String wasteImageUrl;

    @NotNull(message = "La latitude ne peut pas être nulle.")
    private BigDecimal latitude;

    @NotNull(message = "La longitude ne peut pas être nulle.")
    private BigDecimal longitude;

    @NotNull(message = "Le prix pour le collecteur ne peut pas être nul.")
    private BigDecimal collectorPrice;

    // optionnel : si null -> @PrePersist met PENDING
    private Status status;
}