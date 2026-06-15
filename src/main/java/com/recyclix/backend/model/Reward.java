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
@Table(name = "rewards")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reward {

    // =========================================================
    // ID + VARIABLES
    // =========================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "Le nom de la récompense ne peut pas être nul.")
    @Size(max = 150, message = "Le nom ne peut pas dépasser 150 caractères.")
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères.")
    @Column(name = "description", length = 500)
    private String description;

    @NotNull(message = "Le coût en points ne peut pas être nul.")
    @Column(name = "points_cost", nullable = false)
    private Integer pointsCost;

    @Column(name = "monetary_value", precision = 19, scale = 3)
    private BigDecimal monetaryValue;

    @NotNull(message = "La catégorie ne peut pas être nulle.")
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private RewardCategory category;

    @Size(max = 255, message = "L'URL de l'image ne peut pas dépasser 255 caractères.")
    @Column(name = "image_url", length = 255)
    private String imageUrl;

    /**
     * Stock disponible.
     * -1 = illimité
     * 0 = épuisé
     * N = stock restant
     */
    @Column(name = "stock", nullable = false)
    @Builder.Default
    private Integer stock = -1;

    @NotNull
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Size(max = 100, message = "Le nom du partenaire ne peut pas dépasser 100 caractères.")
    @Column(name = "partner_name", length = 100)
    private String partnerName;

    @Column(name = "validity_days")
    private Integer validityDays;

    @Size(max = 500, message = "Les instructions de livraison ne peuvent pas dépasser 500 caractères.")
    @Column(name = "delivery_instructions", length = 500)
    private String deliveryInstructions;

    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Setter(AccessLevel.NONE)
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // =========================================================
    // RELATIONS
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnore
    private FactoryUser createdBy;

    @OneToMany(mappedBy = "reward", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<RewardRedemption> redemptions = new ArrayList<>();

    // =========================================================
    // ENUMS
    // =========================================================

    public enum RewardCategory {
        RECHARGE_PHONE,
        SHOPPING,
        TRANSPORT,
        DONATION,
        CASH_WITHDRAWAL,
        DIGITAL_CODE,
        PHYSICAL_GIFT,
        OTHER
    }

    // =========================================================
    // CALLBACKS
    // =========================================================

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (this.isActive == null) this.isActive = true;
        if (this.stock == null) this.stock = -1;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // =========================================================
    // MÉTHODES MÉTIER
    // =========================================================

    public boolean isInStock() {
        if (this.stock == -1) return true;  // illimité
        return this.stock > 0;
    }

    public void decrementStock() {
        if (this.stock > 0) {
            this.stock--;
        }
    }

    public void incrementStock() {
        if (this.stock >= 0) {
            this.stock++;
        }
    }
}