package com.recyclix.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "faq_entries",
        indexes = {
                @Index(name = "idx_faq_role", columnList = "role_type"),
                @Index(name = "idx_faq_category", columnList = "category_key"),
                @Index(name = "idx_faq_status", columnList = "status"),
                @Index(name = "idx_faq_order", columnList = "display_order")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaqEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "id")
    private Long id;

    // CITIZEN / COLLECTOR
    @NotNull(message = "Le rôle ne peut pas être nul.")
    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", nullable = false, length = 30)
    private RoleType roleType;

    // clé catégorie (ex: "pay_points", "tracking", "missions", ...)
    @NotNull(message = "La catégorie ne peut pas être nulle.")
    @Size(min = 2, max = 80, message = "La catégorie doit être entre 2 et 80 caractères.")
    @Column(name = "category_key", nullable = false, length = 80)
    private String categoryKey;

    // label affiché (ex: "Paiement & Points")
    @NotNull(message = "Le label de catégorie ne peut pas être nul.")
    @Size(min = 2, max = 120, message = "Le label doit être entre 2 et 120 caractères.")
    @Column(name = "category_label", nullable = false, length = 120)
    private String categoryLabel;

    // Question affichée
    @NotNull(message = "La question ne peut pas être nulle.")
    @Size(min = 2, max = 255, message = "La question doit être entre 2 et 255 caractères.")
    @Column(name = "question", nullable = false, length = 255)
    private String question;

    // Réponse affichée
    @NotNull(message = "La réponse ne peut pas être nulle.")
    @Size(min = 2, max = 4000, message = "La réponse doit être entre 2 et 4000 caractères.")
    @Column(name = "answer", nullable = false, length = 4000)
    private String answer;

    // Pour ordonner questions dans la catégorie
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factory_user_id")
    private FactoryUser createdBy;

    public enum RoleType {
        CITIZEN,
        COLLECTOR,
        ALL
    }

    public enum Status {
        ACTIVE,
        INACTIVE
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (this.status == null) this.status = Status.ACTIVE;
        if (this.displayOrder == null) this.displayOrder = 0;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}