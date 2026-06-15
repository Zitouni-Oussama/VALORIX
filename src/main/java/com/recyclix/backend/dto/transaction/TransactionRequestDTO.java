package com.recyclix.backend.dto.transaction;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequestDTO {

    @NotNull(message = "Le accountId ne peut pas être nul.")
    private Long accountId;

    @NotNull(message = "Le type ne peut pas être nul.")
    private String type;

    @NotNull(message = "Le montant ne peut pas être nul.")
    private BigDecimal amount;

    @Size(max = 255, message = "La description ne peut pas dépasser 255 caractères.")
    private String description;

    private Long collectionId;
    private Long paymentId;
}