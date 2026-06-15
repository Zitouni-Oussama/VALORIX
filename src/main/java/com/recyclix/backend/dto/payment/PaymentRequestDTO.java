package com.recyclix.backend.dto.payment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDTO {

    @NotNull(message = "Le accountId ne peut pas être nul.")
    private Long accountId;

    @NotNull(message = "Le montant ne peut pas être nul.")
    private BigDecimal amount;

    @NotNull(message = "La méthode de paiement ne peut pas être nulle.")
    private String method;

    @NotNull(message = "Le statut ne peut pas être nul.")
    private String status;

    @Size(max = 100, message = "Le numéro de référence ne peut pas dépasser 100 caractères.")
    private String referenceNumber;

    private String description;


    private String accountHolderName;
    private String iban;
    private String bankName;
}