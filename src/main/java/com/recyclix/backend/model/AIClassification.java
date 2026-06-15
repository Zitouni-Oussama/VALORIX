package com.recyclix.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ai_classifications")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIClassification {
    //? ID + les variables :
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "Le predicted_material_id ne peut pas être nul.")
    @Column(name = "predicted_material_id", nullable = false)
    private Long predictedMaterialId;

    @NotNull(message = "Le poids prédit ne peut pas être nul.")
    @Column(name = "predicted_weight", nullable = false, precision = 19, scale = 3)
    private BigDecimal predictedWeight;

    @NotNull(message = "Le score de confiance ne peut pas être nul.")
    @Column(name = "confidence_score", nullable = false, precision = 6, scale = 4)
    private BigDecimal confidenceScore;

    @Size(max = 50, message = "La version du modèle IA ne peut pas dépasser 50 caractères.")
    @Column(name = "ai_model_version", length = 50)
    private String aiModelVersion;

    @Column(name = "is_validated", nullable = false)
    private Boolean isValidated;

    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    //*-------------------------------------------------------------
    //? les relations :
    //! --> Factory user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factory_user_id")
    private FactoryUser validatedBy;
    //! --> Collection Request
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false, unique = true)
    @JsonIgnore
    private CollectionRequest request;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.isValidated == null) this.isValidated = false;
    }

    // Optionnel: helper métier
    public void markValidatedBy(FactoryUser factoryUser) {
        this.isValidated = true;
        this.validatedBy = factoryUser;
    }
}