package com.recyclix.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "challenge_history")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChallengeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_challenge_id", nullable = false)
    private UserChallenge userChallenge;

    @NotNull
    @Column(name = "old_progress", nullable = false)
    private Integer oldProgress;

    @NotNull
    @Column(name = "new_progress", nullable = false)
    private Integer newProgress;

    @NotNull
    @Column(name = "change_amount", nullable = false)
    private Integer changeAmount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "change_reason", nullable = false, length = 100)
    private ChangeReason changeReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id")
    private Collection collection;

    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum ChangeReason {
        COLLECTION_COMPLETED,
        COLLECTION_VALIDATED,
        MANUAL_ADJUSTMENT,
        DAILY_STREAK_UPDATE,
        BONUS_AWARDED
    }
}