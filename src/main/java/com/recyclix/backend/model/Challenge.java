package com.recyclix.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "challenges")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "Le titre du défi ne peut pas être nul.")
    @Size(max = 100, message = "Le titre ne peut pas dépasser 100 caractères.")
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères.")
    @Column(name = "description", length = 500)
    private String description;

    @NotNull(message = "Le type de défi ne peut pas être nul.")
    @Enumerated(EnumType.STRING)
    @Column(name = "challenge_type", nullable = false, length = 30)
    private ChallengeType challengeType;

    @Column(name = "target_material_id")
    private Long targetMaterialId;

    @NotNull(message = "La valeur cible ne peut pas être nulle.")
    @Column(name = "target_value", nullable = false)
    private Integer targetValue;

    @NotNull(message = "L'unité ne peut pas être nulle.")
    @Enumerated(EnumType.STRING)
    @Column(name = "unit", nullable = false, length = 20)
    private Unit unit;

    @NotNull(message = "Les points de récompense ne peuvent pas être nuls.")
    @Column(name = "reward_points", nullable = false)
    private Integer rewardPoints;

    @Column(name = "bonus_points")
    @Builder.Default
    private Integer bonusPoints = 0;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @NotNull
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "auto_validate", nullable = false)
    @Builder.Default
    private Boolean autoValidate = true;

    @Column(name = "min_quantity_per_collection", precision = 19, scale = 3)
    private BigDecimal minQuantityPerCollection;

    @Column(name = "max_quantity_per_collection", precision = 19, scale = 3)
    private BigDecimal maxQuantityPerCollection;

    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    private List<UserChallenge> userChallenges = new ArrayList<>();

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.isActive == null) this.isActive = true;
        if (this.autoValidate == null) this.autoValidate = true;
        if (this.bonusPoints == null) this.bonusPoints = 0;
    }

    // ============================================================
    // ENUMS
    // ============================================================

    public enum ChallengeType {
        QUANTITY_BASED,      // Basé sur la quantité totale (kg)
        MATERIAL_SPECIFIC,   // Basé sur un matériau spécifique
        FREQUENCY_BASED,     // Basé sur le nombre de collectes
        STREAK_BASED         // Basé sur une séquence de jours consécutifs
    }

    public enum Unit {
        KG,                  // Kilogrammes
        COLLECTIONS_COUNT,   // Nombre de collectes
        DAYS                 // Jours consécutifs
    }

    // ============================================================
    // MÉTHODES MÉTIER
    // ============================================================

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }

    public boolean isAvailableForMaterial(Long materialId) {
        if (this.challengeType == ChallengeType.MATERIAL_SPECIFIC) {
            return this.targetMaterialId != null && this.targetMaterialId.equals(materialId);
        }
        return true;
    }

    public boolean isCurrentlyActive() {
        LocalDateTime now = LocalDateTime.now();
        boolean started = startDate == null || !now.isBefore(startDate);
        boolean notExpired = endDate == null || !now.isAfter(endDate);
        return started && notExpired && Boolean.TRUE.equals(isActive);
    }

    public int calculateProgressIncrement(BigDecimal quantity, Long materialId, boolean isNewCollection, Integer currentStreak) {
        switch (this.challengeType) {
            case QUANTITY_BASED:
                return validateAndConvertQuantity(quantity);
            case MATERIAL_SPECIFIC:
                if (targetMaterialId != null && targetMaterialId.equals(materialId)) {
                    return validateAndConvertQuantity(quantity);
                }
                return 0;
            case FREQUENCY_BASED:
                return isNewCollection ? 1 : 0;
            case STREAK_BASED:
                return isNewCollection ? 1 : 0;
            default:
                return 0;
        }
    }

    private int validateAndConvertQuantity(BigDecimal quantity) {
        if (quantity == null) return 0;

        if (minQuantityPerCollection != null && quantity.compareTo(minQuantityPerCollection) < 0) {
            return 0;
        }
        if (maxQuantityPerCollection != null && quantity.compareTo(maxQuantityPerCollection) > 0) {
            return (int) Math.floor(maxQuantityPerCollection.doubleValue());
        }

        if (unit == Unit.KG) {
            return (int) Math.floor(quantity.doubleValue());
        }
        return 1;
    }
}