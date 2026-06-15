package com.recyclix.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "payroll_deductions")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollDeduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotNull(message = "Le montant de la déduction est obligatoire.")
    @Positive(message = "Le montant doit être positif.")
    @Column(name = "amount", nullable = false)
    private BigDecimal amount; // Montant retiré en DA

    @NotNull(message = "La date d'application est obligatoire.")
    @Column(name = "deduction_date", nullable = false)
    private LocalDate deductionDate; // Le mois/jour où la déduction s'applique

    @Size(max = 255)
    @Column(name = "reason", length = 255)
    private String reason; // Ex: "Absence non justifiée le 2026-04-20"

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
    }
}