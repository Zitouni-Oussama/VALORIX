package com.recyclix.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Random;

@Getter
@Setter
@Entity
@Table(name = "reward_redemptions")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardRedemption {

    // =========================================================
    // ID + VARIABLES
    // =========================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "Le nombre de points dépensés ne peut pas être nul.")
    @Column(name = "points_spent", nullable = false)
    private Integer pointsSpent;

    @NotNull(message = "Le statut ne peut pas être nul.")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RedemptionStatus status;

    @Size(max = 30, message = "Le code d'échange ne peut pas dépasser 30 caractères.")
    @Column(name = "redemption_code", length = 30, unique = true)
    private String redemptionCode;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Size(max = 500, message = "La note de révision ne peut pas dépasser 500 caractères.")
    @Column(name = "review_note", length = 500)
    private String reviewNote;

    @Size(max = 100, message = "Les informations supplémentaires ne peuvent pas dépasser 100 caractères.")
    @Column(name = "additional_info", length = 100)
    private String additionalInfo;

    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Setter(AccessLevel.NONE)
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // =========================================================
    // RELATIONS
    // =========================================================

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    @JsonIgnore
    private Account account;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reward_id", nullable = false)
    @JsonIgnore
    private Reward reward;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    @JsonIgnore
    private FactoryUser reviewedBy;

    // =========================================================
    // ENUMS
    // =========================================================

    public enum RedemptionStatus {
        PENDING,
        APPROVED,
        REJECTED,
        DELIVERED
    }

    // =========================================================
    // CALLBACKS
    // =========================================================

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (this.status == null) this.status = RedemptionStatus.PENDING;
        if (this.redemptionCode == null) this.redemptionCode = generateRedemptionCode();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // =========================================================
    // MÉTHODES MÉTIER
    // =========================================================

    public void approve(FactoryUser reviewer, String note) {
        this.status = RedemptionStatus.APPROVED;
        this.reviewedBy = reviewer;
        this.reviewNote = note;
    }

    public void reject(FactoryUser reviewer, String reason) {
        this.status = RedemptionStatus.REJECTED;
        this.reviewedBy = reviewer;
        this.reviewNote = reason;
    }

    public void markAsDelivered() {
        this.status = RedemptionStatus.DELIVERED;
    }

    private String generateRedemptionCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder("RX-");
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}