package com.recyclix.backend.dto.financial_report;

import com.recyclix.backend.model.FinancialReport.ReportType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialReportRequestDTO {

    @NotNull(message = "Le type du rapport est obligatoire.")
    private ReportType reportType;

    @NotNull(message = "La date de début est obligatoire.")
    private LocalDate periodStart;

    @NotNull(message = "La date de fin est obligatoire.")
    private LocalDate periodEnd;

    private String filePath;
}