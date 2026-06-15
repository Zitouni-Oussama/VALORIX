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
@Table(name = "machine_incidents")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MachineIncident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "id")
    private Long id;

    @Size(max = 255, message = "Le type de l'incident ne peut pas dépasser 255 caractères.")
    @Column(name = "issue_type", length = 255)
    private String issueType;

    @NotNull(message = "La sévérité ne peut pas être nulle.")
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private IncidentSeverity severity;

    @NotNull(message = "Le statut ne peut pas être nul.")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private IncidentStatus status;

    @Size(max = 255, message = "L'URL de l'image de l'incident ne peut pas dépasser 255 caractères.")
    @Column(name = "incident_image_url", length = 255)
    private String incidentImageUrl;

    @Setter(AccessLevel.NONE)
    @Column(name = "reported_at", nullable = false, updatable = false)
    private LocalDateTime reportedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "machine_id", nullable = false)
    @JsonIgnore
    private Machine machine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by")
    @JsonIgnore
    private FactoryUser reportedBy;

    @PrePersist
    void onCreate() {
        if (this.reportedAt == null) this.reportedAt = LocalDateTime.now();
        if (this.status == null) this.status = IncidentStatus.OPEN;
    }

    // ✅ Méthodes métier propres
    public void startProgress() {
        this.status = IncidentStatus.IN_PROGRESS;
    }

    public void resolve() {
        this.status = IncidentStatus.RESOLVED;
        if (this.resolvedAt == null) this.resolvedAt = LocalDateTime.now();
    }

    public enum IncidentSeverity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public enum IncidentStatus {
        OPEN,
        IN_PROGRESS,
        RESOLVED
    }
}