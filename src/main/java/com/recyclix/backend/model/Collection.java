package com.recyclix.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "collections")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Collection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "La quantité réelle collectée ne peut pas être nulle.")
    @Column(name = "real_quantity", nullable = false, precision = 19, scale = 3)
    private BigDecimal realQuantity;

    @NotNull(message = "Le prix unitaire congelé ne peut pas être nul.")
    @Column(name = "unit_price_frozen", nullable = false, precision = 19, scale = 3)
    private BigDecimal unitPriceFrozen;

    @NotNull(message = "Le montant total ne peut pas être nul.")
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 3)
    private BigDecimal totalAmount;

    @Size(max = 255, message = "L'URL de la preuve de collecte ne peut pas dépasser 255 caractères.")
    @Column(name = "collection_proof_image_url", length = 255)
    private String collectionProofImageUrl;

    @NotNull(message = "Le statut du paiement ne peut pas être nul.")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Setter(AccessLevel.NONE)
    @Column(name = "collected_at", nullable = false)
    private LocalDateTime collectedAt;

    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false, unique = true)
    @JsonIgnore
    private CollectionRequest request;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collector_id", nullable = false)
    @JsonIgnore
    private Collector collector;

    @OneToOne(mappedBy = "collection", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    @JsonIgnore
    private FactoryDelivery factoryDelivery;

    @OneToOne(mappedBy = "collection", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    @JsonIgnore
    private Transaction transaction;

    @OneToOne(mappedBy = "collection", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    @JsonIgnore
    private PointMovement pointMovement;

    @Column(name = "rating", precision = 3, scale = 2)
    private BigDecimal rating;  // Note de 0.00 à 5.00 pour cette collecte

    @Size(max = 500, message = "Le commentaire ne peut pas dépasser 500 caractères.")
    @Column(name = "feedback_comment", length = 500)
    private String feedbackComment;

    @Column(name = "rated_at")
    private LocalDateTime ratedAt;

    @Column(name = "collector_unit_price", precision = 19, scale = 3)
    private BigDecimal collectorUnitPrice;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();

        // Par défaut: si personne ne met collectedAt, on le met à maintenant
        if (this.collectedAt == null) this.collectedAt = LocalDateTime.now();

        // Statut par défaut
        if (this.paymentStatus == null) this.paymentStatus = PaymentStatus.UNPAID;
    }

    // Méthode métier recommandée
    public void markPaid() {
        this.paymentStatus = PaymentStatus.PAID;
    }

    public enum PaymentStatus {
        PAID,
        UNPAID
    }
}