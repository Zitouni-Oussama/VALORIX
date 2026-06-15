package com.recyclix.backend.mapper;

import com.recyclix.backend.dto.financial_report.FinancialReportResponseDTO;
import com.recyclix.backend.dto.financial_report.FinancialReportSummaryDTO;
import com.recyclix.backend.model.FinancialReport;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FinancialReportMapper {

    // ========================= RESPONSE =========================
    @Mappings({
            @Mapping(
                    target = "generatedById",
                    expression = "java(entity.getGeneratedBy() != null ? entity.getGeneratedBy().getId() : null)"
            ),
            @Mapping(
                    target = "generatedByFullName",
                    expression = "java(entity.getGeneratedBy() != null ? entity.getGeneratedBy().getFirstName() + \" \" + entity.getGeneratedBy().getLastName() : null)"
            ),
            // ✅ AJOUTER CES CHAMPS POUR ÉVITER LES PROBLÈMES DE LAZY
            @Mapping(target = "revenueFromFactories", ignore = true),
            @Mapping(target = "totalExpenses", ignore = true),
            @Mapping(target = "estimatedPointCosts", ignore = true),
            @Mapping(target = "netBalance", ignore = true),
            @Mapping(target = "healthStatus", ignore = true)
    })
    FinancialReportResponseDTO toResponseDTO(FinancialReport entity);

    // ========================= SUMMARY =========================
    @Mapping(
            target = "generatedById",
            expression = "java(entity.getGeneratedBy() != null ? entity.getGeneratedBy().getId() : null)"
    )
    FinancialReportSummaryDTO toSummaryDTO(FinancialReport entity);
}