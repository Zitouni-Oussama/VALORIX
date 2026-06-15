// dto/transaction/WithdrawalRequestDTO.java
package com.recyclix.backend.dto.transaction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WithdrawalRequestDTO {

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à 0")
    private BigDecimal amount;

    @NotBlank(message = "Le nom du titulaire du compte est obligatoire")
    private String accountHolderName;

    @NotBlank(message = "L'IBAN est obligatoire")
    private String iban;

    @NotBlank(message = "Le nom de la banque est obligatoire")
    private String bankName;
}