package com.recyclix.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "factory_deliveries")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FactoryDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "Le statut de la livraison ne peut pas être nul.")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DeliveryStatus status;

    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;

    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collection_id", nullable = false, unique = true)
    @JsonIgnore
    private Collection collection;

    @OneToOne(mappedBy = "delivery", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    @JsonIgnore
    private FactoryValidation validation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recycling_center_id")
    @JsonIgnore
    private RecyclingCenter recyclingCenter;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = DeliveryStatus.PENDING;
    }

    // Optionnel: méthodes métier propres
    public void markValidated() {
        this.status = DeliveryStatus.VALIDATED;
        if (this.deliveryDate == null) this.deliveryDate = LocalDateTime.now();
    }

    public void markRefused() {
        this.status = DeliveryStatus.REFUSED;
    }

    public enum DeliveryStatus {
        PENDING,
        VALIDATED,
        ADJUSTED,
        REFUSED,
        PROCESSING,
        COMPLETED
    }
}