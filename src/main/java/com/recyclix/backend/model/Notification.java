package com.recyclix.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "notifications")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "Le titre de la notification ne peut pas être nul.")
    @Size(max = 255, message = "Le titre ne peut pas dépasser 255 caractères.")
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Size(max = 500, message = "Le message de la notification ne peut pas dépasser 500 caractères.")
    @Column(name = "message", length = 500)
    private String message;

    @NotNull(message = "Le type de la notification ne peut pas être nul.")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private NotificationType type;

    @NotNull
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id")
    @JsonIgnore
    private Account account;


    @Enumerated(EnumType.STRING)
    @Column(name = "target_role", nullable = false, length = 20)
    private RoleTypeN targetRole;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.isRead == null) this.isRead = false;
    }

    public enum RoleTypeN {
        CITIZEN,
        COLLECTOR,
        ALL
    }

    // Optionnel: méthode métier
    public void markAsRead() {
        this.isRead = true;
    }

    public enum NotificationType {
        INFO,
        WARNING,
        SUCCESS,
        ERROR
    }
}