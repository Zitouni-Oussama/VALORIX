package com.recyclix.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "collector_location_history")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectorLocationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "id")
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @Setter(AccessLevel.NONE) // recommandé: ne pas changer le collector associé
    @JoinColumn(name = "collector_id", nullable = false, unique = true)
    private Collector collector;

    @NotNull(message = "La latitude ne peut pas être nulle.")
    @Column(name = "latitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @NotNull(message = "La longitude ne peut pas être nulle.")
    @Column(name = "longitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Setter(AccessLevel.NONE)
    @Column(name = "recorded_at", nullable = false, updatable = false)
    private LocalDateTime recordedAt;

    @PrePersist
    void onCreate() {
        if (this.recordedAt == null) this.recordedAt = LocalDateTime.now();
    }
}