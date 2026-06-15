package com.recyclix.backend.dto.financial_report;

import com.recyclix.backend.model.FinancialReport.ReportType;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialReportUpdateDTO {

    private ReportType reportType;

    private LocalDate periodStart;
    private LocalDate periodEnd;

    private String filePath;
}