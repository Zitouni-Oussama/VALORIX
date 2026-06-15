package com.recyclix.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "user_challenges",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_challenge_account_challenge",
                        columnNames = {"account_id", "challenge_id"})
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "La quantité de progression ne peut pas être nulle.")
    @Builder.Default
    @Column(name = "progress_quantity", nullable = false)
    private Integer progressQuantity = 0;

    @NotNull(message = "Le statut du défi ne peut pas être nul.")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ChallengeStatus status;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // NOUVEAUX CHAMPS
    @Column(name = "last_activity_date")
    private LocalDateTime lastActivityDate;

    @Builder.Default
    @Column(name = "current_streak")
    private Integer currentStreak = 0;

    @Builder.Default
    @Column(name = "best_streak")
    private Integer bestStreak = 0;

    @Builder.Default
    @Column(name = "points_awarded")
    private Boolean pointsAwarded = false;

    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Setter(AccessLevel.NONE)
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @Setter(AccessLevel.NONE)
    @JoinColumn(name = "account_id", nullable = false)
    @JsonIgnore
    private Account account;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @Setter(AccessLevel.NONE)
    @JoinColumn(name = "challenge_id", nullable = false)
    @JsonIgnore
    private Challenge challenge;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (this.status == null) this.status = ChallengeStatus.IN_PROGRESS;
        if (this.progressQuantity == null) this.progressQuantity = 0;
        if (this.currentStreak == null) this.currentStreak = 0;
        if (this.bestStreak == null) this.bestStreak = 0;
        if (this.pointsAwarded == null) this.pointsAwarded = false;
        if (this.status == ChallengeStatus.COMPLETED && this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        }
        if (this.lastActivityDate == null && this.createdAt != null) {
            this.lastActivityDate = this.createdAt;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.status == ChallengeStatus.COMPLETED && this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        }
    }

    // ============================================================
    // MÉTHODES MÉTIER
    // ============================================================

    public void addProgress(int increment, Long collectionId, ChallengeHistory.ChangeReason reason) {
        if (increment <= 0) return;
        if (this.status != ChallengeStatus.IN_PROGRESS) return;

        int oldProgress = this.progressQuantity;
        this.progressQuantity += increment;

        if (this.progressQuantity >= this.challenge.getTargetValue()) {
            this.progressQuantity = this.challenge.getTargetValue();
            this.status = ChallengeStatus.COMPLETED;
            this.completedAt = LocalDateTime.now();
        }

        this.lastActivityDate = LocalDateTime.now();
    }

    public void updateStreak(boolean hasCollectionToday, Long collectionId) {
        if (this.challenge.getChallengeType() != Challenge.ChallengeType.STREAK_BASED) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        boolean isConsecutive = isActivityConsecutive(now);

        if (hasCollectionToday) {
            if (isConsecutive) {
                this.currentStreak++;
            } else {
                this.currentStreak = 1;
            }

            if (this.currentStreak > this.bestStreak) {
                this.bestStreak = this.currentStreak;
            }

            this.lastActivityDate = now;
            this.progressQuantity = this.currentStreak;

            if (this.currentStreak >= this.challenge.getTargetValue()) {
                this.status = ChallengeStatus.COMPLETED;
                this.completedAt = now;
                this.progressQuantity = this.challenge.getTargetValue();
            }
        } else {
            if (!isConsecutive && this.currentStreak > 0) {
                this.currentStreak = 0;
                this.progressQuantity = 0;
            }
        }
    }

    private boolean isActivityConsecutive(LocalDateTime now) {
        if (this.lastActivityDate == null) return false;

        LocalDateTime lastDateMidnight = this.lastActivityDate.toLocalDate().atStartOfDay();
        LocalDateTime nowMidnight = now.toLocalDate().atStartOfDay();

        long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(lastDateMidnight, nowMidnight);
        return daysDiff == 1;
    }

    public void markPointsAwarded() {
        this.pointsAwarded = true;
    }

    // ============================================================
    // ENUMS
    // ============================================================

    public enum ChallengeStatus {
        IN_PROGRESS,
        COMPLETED
    }
}