package com.recyclix.backend.dto.factory_invoice;

import com.recyclix.backend.model.FactoryInvoice.InvoiceStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FactoryInvoiceRequestDTO {

    // =========================================================
    // INFORMATIONS FACTURE
    // =========================================================

    @NotBlank(message = "Le nom de l'usine est obligatoire.")
    @Size(max = 150)
    private String factoryName;

    @Size(max = 255)
    private String factoryAddress;

    @Size(max = 50)
    private String factoryTaxId;

    // =========================================================
    // MONTANTS
    // =========================================================

    @NotNull(message = "Le montant HT est obligatoire.")
    @DecimalMin(value = "0.01", message = "Le montant HT doit être supérieur à 0")
    private BigDecimal amountHt;

    @NotNull(message = "Le taux de TVA est obligatoire.")
    @DecimalMin(value = "0.00", message = "Le taux TVA doit être >= 0")
    @DecimalMax(value = "0.30", message = "Le taux TVA maximum est 30%")
    private BigDecimal tvaRate;

    // =========================================================
    // DATES
    // =========================================================

    private LocalDate issueDate;

    @Future(message = "La date d'échéance doit être dans le futur")
    private LocalDate dueDate;

    // =========================================================
    // STATUT
    // =========================================================

    private InvoiceStatus status;

    // =========================================================
    // CONDITIONS
    // =========================================================

    @Size(max = 500)
    private String paymentTerms;

    @Size(max = 1000)
    private String notes;

    // =========================================================
    // BANQUE
    // =========================================================

    @Size(max = 50)
    private String bankIban;

    @Size(max = 20)
    private String bankBic;

    // =========================================================
    // COLLECTES
    // =========================================================

    private BigDecimal totalWeightKg;
    private Integer collectionsCount;
}