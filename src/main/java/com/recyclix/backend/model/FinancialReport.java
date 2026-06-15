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
@Table(name = "financial_reports")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "Le type de rapport ne peut pas être nul.")
    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 20)
    private ReportType reportType;

    @NotNull(message = "La date de début de la période ne peut pas être nulle.")
    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @NotNull(message = "La date de fin de la période ne peut pas être nulle.")
    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @NotNull(message = "Le revenu total ne peut pas être nul.")
    @Column(name = "total_income", nullable = false, precision = 19, scale = 3)
    private BigDecimal totalIncome;

    @NotNull(message = "La dépense totale ne peut pas être nulle.")
    @Column(name = "total_expense", nullable = false, precision = 19, scale = 3)
    private BigDecimal totalExpense;

    @NotNull(message = "Le profit net ne peut pas être nul.")
    @Column(name = "net_profit", nullable = false, precision = 19, scale = 3)
    private BigDecimal netProfit;

    @Setter(AccessLevel.NONE)
    @Column(name = "generated_at", nullable = false, updatable = false)
    private LocalDateTime generatedAt;

    @Size(max = 255, message = "Le chemin du fichier ne peut pas dépasser 255 caractères.")
    @Column(name = "file_path", length = 255)
    private String filePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by")
    @JsonIgnore
    private FactoryUser generatedBy;

    @PrePersist
    void onCreate() {
        this.generatedAt = LocalDateTime.now();

        // Optionnel: si netProfit est null, on le calcule automatiquement
        if (this.netProfit == null && this.totalIncome != null && this.totalExpense != null) {
            this.netProfit = this.totalIncome.subtract(this.totalExpense);
        }
    }

    public enum ReportType {
        DAILY,
        WEEKLY,
        MONTHLY,
        YEARLY,
        CUSTOM
    }
}