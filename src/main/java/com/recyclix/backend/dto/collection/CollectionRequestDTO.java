package com.recyclix.backend.dto.collection;

import com.recyclix.backend.model.Collection.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionRequestDTO {

    @NotNull(message = "Le requestId ne peut pas être nul.")
    private Long requestId;

    @NotNull(message = "Le collectorId ne peut pas être nul.")
    private Long collectorId;

    @NotNull(message = "La quantité réelle collectée ne peut pas être nulle.")
    private BigDecimal realQuantity;

    @NotNull(message = "Le prix unitaire congelé ne peut pas être nul.")
    private BigDecimal unitPriceFrozen;

    @NotNull(message = "Le montant total ne peut pas être nul.")
    private BigDecimal totalAmount;

    @Size(max = 255, message = "L'URL de la preuve de collecte ne peut pas dépasser 255 caractères.")
    private String collectionProofImageUrl;

    // optionnel : si null -> @PrePersist met UNPAID
    private PaymentStatus paymentStatus;
}