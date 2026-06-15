package com.recyclix.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "point_movements")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "Le type de mouvement ne peut pas être nul.")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private PointMovementType type;

    @NotNull(message = "Le nombre de points ne peut pas être nul.")
    @Column(name = "points_amount", nullable = false)
    private Integer pointsAmount;

    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @Setter(AccessLevel.NONE) // recommandé: le mouvement appartient à un seul compte
    @JoinColumn(name = "account_id", nullable = false)
    @JsonIgnore
    private Account account;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", unique = true)
    @JsonIgnore
    private Collection collection;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum PointMovementType {
        EARN,
        CONVERT,
        BONUS,
        CHALLENGE_REWARD
    }
}