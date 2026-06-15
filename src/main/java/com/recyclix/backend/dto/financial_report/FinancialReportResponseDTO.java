package com.recyclix.backend.dto.financial_report;

import com.recyclix.backend.model.FinancialReport.ReportType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialReportResponseDTO {

    private Long id;

    private ReportType reportType;

    private LocalDate periodStart;
    private LocalDate periodEnd;

    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netProfit;

    private BigDecimal revenueFromFactories;
    private BigDecimal totalExpenses;
    private BigDecimal estimatedPointCosts;
    private BigDecimal netBalance;

    private String healthStatus;
    private String filePath;

    private LocalDateTime generatedAt;

    private Long generatedById;
    private String generatedByFullName;
}