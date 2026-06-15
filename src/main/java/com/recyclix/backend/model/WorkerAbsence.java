package com.recyclix.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "worker_absences")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerAbsence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotNull(message = "Le type d'absence ne peut pas être nul.")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private AbsenceType type;

    @NotNull(message = "La date de début ne peut pas être nulle.")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Size(max = 255, message = "La raison de l'absence ne peut pas dépasser 255 caractères.")
    @Column(name = "reason", length = 255)
    private String reason;

    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by")
    @JsonIgnore
    private FactoryUser recordedBy;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();

        // Optionnel: cohérence si endDate < startDate
        if (this.endDate != null && this.endDate.isBefore(this.startDate)) {
            throw new IllegalArgumentException("endDate ne peut pas être avant startDate");
        }
    }

    public enum AbsenceType {
        ABSENCE,
        JUSTIFIED,
        LATE
    }
}