package com.recyclix.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "factory_invoices", indexes = {
        @Index(name = "idx_invoice_reference", columnList = "reference_number"),
        @Index(name = "idx_invoice_factory", columnList = "factory_name"),
        @Index(name = "idx_invoice_status", columnList = "status"),
        @Index(name = "idx_invoice_date", columnList = "issue_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class FactoryInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================================================
    // INFORMATIONS FACTURE
    // =========================================================

    @Column(name = "reference_number", unique = true, nullable = false, length = 50)
    private String referenceNumber;

    @NotNull(message = "Le nom de l'usine est obligatoire.")
    @Column(name = "factory_name", nullable = false, length = 150)
    private String factoryName;

    @Column(name = "factory_address", length = 255)
    private String factoryAddress;

    @Column(name = "factory_tax_id", length = 50)
    private String factoryTaxId;  // NIF (Numéro d'Identification Fiscale)

    // =========================================================
    // MONTANTS ET TVA
    // =========================================================

    @NotNull(message = "Le montant HT est obligatoire.")
    @Column(name = "amount_ht", nullable = false, precision = 19, scale = 3)
    private BigDecimal amountHt;

    @NotNull(message = "Le taux de TVA est obligatoire.")
    @Column(name = "tva_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal tvaRate;  // 0.00, 0.09, 0.19

    @Column(name = "tva_amount", precision = 19, scale = 3)
    private BigDecimal tvaAmount;

    @Column(name = "amount_ttc", precision = 19, scale = 3)
    private BigDecimal amountTtc;

    // =========================================================
    // DATES
    // =========================================================

    @NotNull(message = "La date d'émission est obligatoire.")
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    // =========================================================
    // STATUT
    // =========================================================

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InvoiceStatus status;

    // =========================================================
    // CONDITIONS ET NOTES
    // =========================================================

    @Column(name = "payment_terms", length = 500)
    private String paymentTerms;  // "Paiement sous 30 jours", etc.

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "bank_iban", length = 50)
    private String bankIban;

    @Column(name = "bank_bic", length = 20)
    private String bankBic;

    // =========================================================
    // AUDIT
    // =========================================================

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    // =========================================================
    // DETAILS DES COLLECTES (optionnel)
    // =========================================================

    @Column(name = "total_weight_kg", precision = 19, scale = 3)
    private BigDecimal totalWeightKg;

    @Column(name = "collections_count")
    private Integer collectionsCount;

    // =========================================================
    // ENUMS
    // =========================================================

    public enum InvoiceStatus {
        DRAFT,      // Brouillon
        PENDING,    // En attente de paiement
        PAID,       // Payée
        OVERDUE,    // En retard
        CANCELLED,  // Annulée
        REFUNDED    // Remboursée
    }

    // =========================================================
    // MÉTHODES DE CALCUL
    // =========================================================

    @PrePersist
    @PreUpdate
    public void calculateAmounts() {
        if (amountHt != null && tvaRate != null) {
            // Calcul TVA
            this.tvaAmount = amountHt.multiply(tvaRate)
                    .setScale(3, RoundingMode.HALF_UP);

            // Calcul TTC
            this.amountTtc = amountHt.add(tvaAmount)
                    .setScale(3, RoundingMode.HALF_UP);
        }

        if (issueDate == null) {
            issueDate = LocalDate.now();
        }

        if (status == null) {
            status = InvoiceStatus.DRAFT;
        }

        // Vérifier si la facture est en retard
        if (status == InvoiceStatus.PENDING && dueDate != null && dueDate.isBefore(LocalDate.now())) {
            status = InvoiceStatus.OVERDUE;
        }
    }

    public void markAsPaid() {
        this.status = InvoiceStatus.PAID;
        this.paidDate = LocalDate.now();
    }

    public void markAsOverdue() {
        if (this.status != InvoiceStatus.PAID) {
            this.status = InvoiceStatus.OVERDUE;
        }
    }

    // =========================================================
    // GÉNÉRATION NUMÉRO FACTURE
    // =========================================================

    public static String generateReferenceNumber() {
        String year = String.valueOf(LocalDate.now().getYear());
        String month = String.format("%02d", LocalDate.now().getMonthValue());
        String day = String.format("%02d", LocalDate.now().getDayOfMonth());

        // Format: FACT-2024-05-03-XXXXX
        // Le séquentiel sera géré par la base de données ou un compteur
        return String.format("FACT-%s%s%s-", year, month, day);
    }
}