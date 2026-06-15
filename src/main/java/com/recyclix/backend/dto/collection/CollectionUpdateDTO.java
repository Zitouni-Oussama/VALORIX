package com.recyclix.backend.dto.collection;

import com.recyclix.backend.model.Collection.PaymentStatus;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionUpdateDTO {

    private BigDecimal realQuantity;
    private BigDecimal unitPriceFrozen;
    private BigDecimal totalAmount;

    @Size(max = 255, message = "L'URL de la preuve de collecte ne peut pas dépasser 255 caractères.")
    private String collectionProofImageUrl;

    private PaymentStatus paymentStatus;
}