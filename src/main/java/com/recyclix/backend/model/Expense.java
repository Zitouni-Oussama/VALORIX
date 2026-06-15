package com.recyclix.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "expenses")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "La catégorie ne peut pas être nulle.")
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private ExpenseCategory category;

    @Size(max = 255, message = "La description ne peut pas dépasser 255 caractères.")
    @Column(name = "description", length = 255)
    private String description;

    @NotNull(message = "Le montant ne peut pas être nul.")
    @Column(name = "amount", nullable = false, precision = 19, scale = 3)
    private BigDecimal amount;

    @NotNull(message = "La date de la dépense ne peut pas être nulle.")
    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnore
    private FactoryUser createdBy;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.expenseDate == null) this.expenseDate = LocalDate.now();
    }

    public enum ExpenseCategory {
        SALARY,
        MAINTENANCE,
        MARKETING,
        UTILITIES,
        OTHER,
        COLLECTOR_PAYMENT,
        CLIENT_PAYMENT
    }
}