package com.recyclix.backend.dto.factory_invoice;

import com.recyclix.backend.model.FactoryInvoice.InvoiceStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FactoryInvoiceResponseDTO {

    private Long id;
    private String referenceNumber;

    // Informations usine
    private String factoryName;
    private String factoryAddress;
    private String factoryTaxId;

    // Montants
    private BigDecimal amountHt;
    private BigDecimal tvaRate;
    private BigDecimal tvaAmount;
    private BigDecimal amountTtc;

    // Dates
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate paidDate;

    // Statut
    private InvoiceStatus status;
    private String statusLabel;
    private String statusColor;

    // Conditions
    private String paymentTerms;
    private String notes;
    private String bankIban;
    private String bankBic;

    // Collectes
    private BigDecimal totalWeightKg;
    private Integer collectionsCount;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
}