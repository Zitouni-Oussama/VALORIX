package com.recyclix.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "trucks")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Truck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "id")
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @Setter(AccessLevel.NONE) // recommandé : un truck appartient à un collector fixe
    @JoinColumn(name = "collector_id", nullable = false, unique = true)
    private Collector collector;

    @NotNull(message = "Le numéro de plaque ne peut pas être nul.")
    @Size(max = 50, message = "Le numéro de plaque ne peut pas dépasser 50 caractères.")
    @Column(name = "plate_number", nullable = false, unique = true, length = 50)
    private String plateNumber;

    @Size(max = 100, message = "La marque ne peut pas dépasser 100 caractères.")
    @Column(name = "brand", length = 100)
    private String brand;

    @Size(max = 100, message = "Le modèle ne peut pas dépasser 100 caractères.")
    @Column(name = "model", length = 100)
    private String model;

    @Size(max = 255, message = "L'URL de la photo du camion ne peut pas dépasser 255 caractères.")
    @Column(name = "truck_photo_url", length = 255)
    private String truckPhotoUrl;

    @Size(max = 255, message = "L'URL de la carte grise ne peut pas dépasser 255 caractères.")
    @Column(name = "grey_card_image_url", length = 255)
    private String greyCardImageUrl;

    @Size(max = 255, message = "L'URL du permis de conduire ne peut pas dépasser 255 caractères.")
    @Column(name = "driving_license_image_url", length = 255)
    private String drivingLicenseImageUrl;

    @NotNull
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "capacity_Kg")
    private double capacityKg;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.isActive == null) this.isActive = true;
    }

    // Optionnel: méthodes métier
    public void activate() { this.isActive = true; }
    public void deactivate() { this.isActive = false; }

}