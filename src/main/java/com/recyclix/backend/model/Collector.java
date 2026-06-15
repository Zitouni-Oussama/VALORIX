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
@Table(name = "collectors")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Collector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "id")
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @Setter(AccessLevel.NONE) // recommandé: un collector ne change pas de compte
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

    @NotNull(message = "Le numéro d'identification national ne peut pas être nul.")
    @Size(max = 50, message = "Le numéro d'identification national ne peut pas dépasser 50 caractères.")
    @Column(name = "national_id_number", nullable = false, length = 50)
    private String nationalIdNumber;

    @NotNull
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified;

    // ex: note moyenne 0.00 -> 5.00
    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Column(name = "current_latitude", precision = 10, scale = 7)
    private BigDecimal currentLatitude;

    @Column(name = "current_longitude", precision = 10, scale = 7)
    private BigDecimal currentLongitude;

    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ⚠️ À garder optional=false SEULEMENT si tu crées toujours truck au même moment
    @OneToOne(mappedBy = "collector", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private Truck truck;

    // ⚠️ Souvent c'est optionnel au départ aussi
    @OneToOne(mappedBy = "collector", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private CollectorLocationHistory collectorLocationHistory;

    @OneToMany(mappedBy = "collector", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Collection> collections = new ArrayList<>();

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.isVerified == null) this.isVerified = false;
        if (this.averageRating == null) this.averageRating = BigDecimal.ZERO; // optionnel
    }

    // Méthode métier (optionnel)
    public void verify() {
        this.isVerified = true;
    }
}