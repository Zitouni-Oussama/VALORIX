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
@Table(name = "clients")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "id")
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @Setter(AccessLevel.NONE) // optionnel: bloque le changement de compte
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    @JsonIgnore
    private Account account;

    @NotNull(message = "Le prénom ne peut pas être nul.")
    @Size(max = 100, message = "Le prénom ne peut pas dépasser 100 caractères.")
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotNull(message = "Le nom ne peut pas être nul.")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères.")
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Size(max = 255, message = "L'adresse ne peut pas dépasser 255 caractères.")
    @Column(name = "address", length = 255)
    private String address;

    // Optionnel mais recommandé (coords)
    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @NotNull(message = "Le nombre total de points ne peut pas être nul.")
    @Column(name = "total_points", nullable = false)
    private Integer totalPoints;

    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<CollectionRequest> collectionRequests = new ArrayList<>();

    //! --> Leaderboards
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Leaderboard> leaderboards = new ArrayList<>();

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.totalPoints == null) this.totalPoints = 0;
    }

    // Optionnel: méthode métier pour les points
    public void addPoints(int points) {
        if (points < 0) throw new IllegalArgumentException("points doit être >= 0");
        this.totalPoints = (this.totalPoints == null ? 0 : this.totalPoints) + points;
    }
}