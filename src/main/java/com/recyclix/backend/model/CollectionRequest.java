package com.recyclix.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Getter
@Setter
@Entity
@Table(name = "collection_requests")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "La quantité estimée ne peut pas être nulle.")
    @Column(name = "estimated_quantity", nullable = false, precision = 19, scale = 3)
    private BigDecimal estimatedQuantity;

    @NotNull(message = "Le montant estimé ne peut pas être nul.")
    @Column(name = "estimated_amount", nullable = false, precision = 19, scale = 3)
    private BigDecimal estimatedAmount;

    @Size(max = 255, message = "L'URL de l'image des déchets ne peut pas dépasser 255 caractères.")
    @Column(name = "waste_image_url", length = 255)
    private String wasteImageUrl;

    @NotNull(message = "La latitude ne peut pas être nulle.")
    @Column(name = "latitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @NotNull(message = "La longitude ne peut pas être nulle.")
    @Column(name = "longitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @NotNull(message = "Le statut ne peut pas être nul.")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status;

    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Setter(AccessLevel.NONE)
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @NotNull(message = "Le prix pour le collecteur ne peut pas être nul.")
    @Column(name = "collector_price", nullable = false, precision = 19, scale = 3)
    private BigDecimal collectorPrice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    @JsonIgnore
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id")
    @JsonIgnore
    private Material material;

    @OneToOne(mappedBy = "request", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    @JsonIgnore
    private AIClassification aiClassification;

    @OneToOne(mappedBy = "request", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    @JsonIgnore
    private Collection collection;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (this.status == null) this.status = Status.PENDING;

        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (this.status == null) this.status = Status.PENDING;
        if (this.codeStatus == null) this.codeStatus = CodeStatus.PENDING;
        if (this.validationCode == null) {
            this.validationCode = generateValidationCode();
            this.codeGeneratedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


    public enum Status {

        // CLIENT
        PENDING,        // demande créée
        CANCELLED,
        EXPIRED,

        // COLLECTEUR
        ACCEPTED,       // collecteur a accepté
        COLLECTED,      // déchets récupérés
        DELIVERED,

        ADMIN_CANCELLED
    }

    // Ajouter ces champs dans la classe
    @Column(name = "validation_code", length = 30, unique = true)
    private String validationCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "code_status", length = 20)
    private CodeStatus codeStatus;

    @Column(name = "code_generated_at")
    private LocalDateTime codeGeneratedAt;

    @Column(name = "code_validated_at")
    private LocalDateTime codeValidatedAt;

    @Column(name = "validated_by_collector_id")
    private Long validatedByCollectorId;

    // Ajouter l'enum CodeStatus à l'intérieur ou à l'extérieur de la classe
    public enum CodeStatus {
        PENDING,    // Code généré, en attente d'assignation
        ASSIGNED,   // Collecteur assigné à la mission
        USED,       // Code utilisé pour valider la collecte
        EXPIRED     // Code expiré (temps dépassé)
    }

    // Ajouter cette méthode pour générer le code unique
    private String generateValidationCode() {
        // Format: RX-YYMMDD-XXXX (ex: RX-260501-A3F9)
        LocalDateTime now = LocalDateTime.now();
        String datePart = now.format(DateTimeFormatter.ofPattern("yyMMdd"));
        String randomPart = generateRandomCode(4);
        return String.format("RX-%s-%s", datePart, randomPart);
    }

    private String generateRandomCode(int length) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}