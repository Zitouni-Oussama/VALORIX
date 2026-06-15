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
        name = "support_tickets",
        indexes = {
                @Index(name = "idx_ticket_role", columnList = "role_type"),
                @Index(name = "idx_ticket_status", columnList = "status"),
                @Index(name = "idx_ticket_created", columnList = "created_at"),
                @Index(name = "idx_ticket_account", columnList = "account_id")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportTicket {

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

    @Size(max = 150, message = "Le sujet ne peut pas dépasser 150 caractères.")
    @Column(name = "subject", length = 150)
    private String subject;

    @NotNull(message = "Le message ne peut pas être nul.")
    @Size(min = 5, max = 4000, message = "Le message doit être entre 5 et 4000 caractères.")
    @Column(name = "message", nullable = false, length = 4000)
    private String message;

    @NotNull(message = "Le statut ne peut pas être nul.")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status;

    // optionnel: réponse support
    @Size(max = 4000, message = "La réponse ne peut pas dépasser 4000 caractères.")
    @Column(name = "response_message", length = 4000)
    private String responseMessage;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Setter(AccessLevel.NONE)
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // l'utilisateur connecté (si disponible)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", foreignKey = @ForeignKey(name = "fk_ticket_account"))
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factory_user_id")
    private FactoryUser createdBy;

    public enum RoleType {
        CITIZEN,
        COLLECTOR
    }

    public enum Status {
        OPEN,
        IN_PROGRESS,
        CLOSED
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (this.status == null) this.status = Status.OPEN;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}