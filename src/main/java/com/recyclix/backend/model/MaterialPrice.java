package com.recyclix.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "material_prices")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "Le prix pour les citoyens ne peut pas être nul.")
    @Column(name = "citizen_price_per_kg", nullable = false, precision = 19, scale = 3)
    private BigDecimal citizenPricePerKg;

    @NotNull(message = "Le prix pour les collecteurs ne peut pas être nul.")
    @Column(name = "collector_price_per_kg", nullable = false, precision = 19, scale = 3)
    private BigDecimal collectorPricePerKg;

    @NotNull(message = "La date de début ne peut pas être nulle.")
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @Setter(AccessLevel.NONE) // recommandé : un prix est lié à un seul material
    @JoinColumn(name = "material_id", nullable = false, unique = true)
    private Material material;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.startDate == null) this.startDate = LocalDateTime.now();
    }
}