package com.recyclix.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "leaderboards")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Leaderboard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "Le type de période ne peut pas être nul.")
    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 20)
    private PeriodType periodType;

    @NotNull(message = "La date de début de la période ne peut pas être nulle.")
    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @NotNull(message = "La date de fin de la période ne peut pas être nulle.")
    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @NotNull(message = "Le nombre total de points ne peut pas être nul.")
    @Column(name = "total_points", nullable = false)
    private Integer totalPoints;

    @NotNull(message = "La position du classement ne peut pas être nulle.")
    @Column(name = "rank_position", nullable = false)
    private Integer rankPosition;

    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    @JsonIgnore
    private Client client;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.totalPoints == null) this.totalPoints = 0; // optionnel
    }

    public enum PeriodType {
        WEEKLY,
        MONTHLY
    }
}