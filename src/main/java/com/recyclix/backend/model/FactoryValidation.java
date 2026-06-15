package com.recyclix.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "factory_validations")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FactoryValidation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "Le poids déclaré ne peut pas être nul.")
    @Column(name = "declared_weight", nullable = false, precision = 19, scale = 3)
    private BigDecimal declaredWeight;

    @NotNull(message = "Le poids validé ne peut pas être nul.")
    @Column(name = "validated_weight", nullable = false, precision = 19, scale = 3)
    private BigDecimal validatedWeight;

    @Column(name = "adjustment_note", length = 500)
    private String adjustmentNote;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Setter(AccessLevel.NONE)
    @Column(name = "validated_at", nullable = false, updatable = false)
    private LocalDateTime validatedAt;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @Setter(AccessLevel.NONE) // recommandé : une validation est liée à une livraison fixe
    @JoinColumn(name = "delivery_id", nullable = false, unique = true)
    @JsonIgnore
    private FactoryDelivery delivery;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @Setter(AccessLevel.NONE) // recommandé : qui a validé ne doit pas changer
    @JoinColumn(name = "validated_by", nullable = false)
    @JsonIgnore
    private FactoryUser validatedBy;

    // Ajout dans la classe FactoryValidation
    @Column(name = "paid", nullable = false)
    private Boolean paid = false;

    @Column(name = "collector_amount", precision = 19, scale = 3)
    private BigDecimal collectorAmount;

    @PrePersist
    void onCreate() {
        if (this.validatedAt == null) this.validatedAt = LocalDateTime.now();
    }
}