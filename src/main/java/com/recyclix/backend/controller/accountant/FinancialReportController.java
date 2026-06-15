//package com.recyclix.backend.controller.accountant;
//
//import com.recyclix.backend.dto.financial_report.FinancialReportRequestDTO;
//import com.recyclix.backend.dto.financial_report.FinancialReportResponseDTO;
//import com.recyclix.backend.dto.financial_report.FinancialReportSummaryDTO;
//import com.recyclix.backend.dto.financial_report.FinancialReportUpdateDTO;
//import com.recyclix.backend.model.FinancialReport;
//import com.recyclix.backend.service.accountant.FinancialReportService;
//import com.recyclix.backend.util.ApiResponse;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.format.annotation.DateTimeFormat;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDate;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/accountant/reports")
//@PreAuthorize("@factoryAccess.hasPosition('ACCOUNTANT')")
//@RequiredArgsConstructor
//public class FinancialReportController {
//
//    private final FinancialReportService financialReportService;
//
//    @PostMapping("/generate")
//    public ApiResponse<FinancialReportResponseDTO> generateAndSaveReport(
//            @Valid @RequestBody FinancialReportRequestDTO request
//    ) {
//        return ApiResponse.ok(
//                "Rapport financier généré et sauvegardé avec succès.",
//                financialReportService.generateAndSaveReport(request)
//        );
//    }
//
//    @GetMapping("/preview")
//    public ApiResponse<FinancialReportResponseDTO> previewReport(
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
//    ) {
//        return ApiResponse.ok(
//                "Aperçu du rapport financier généré avec succès.",
//                financialReportService.previewReport(startDate, endDate)
//        );
//    }
//
//    @GetMapping
//    public ApiResponse<List<FinancialReportSummaryDTO>> getAllReports() {
//        return ApiResponse.ok(
//                "Liste des rapports financiers récupérée.",
//                financialReportService.getAllReports()
//        );
//    }
//
//    @GetMapping("/{id}")
//    public ApiResponse<FinancialReportResponseDTO> getReportById(@PathVariable Long id) {
//        return ApiResponse.ok(
//                "Rapport financier récupéré avec succès.",
//                financialReportService.getReportById(id)
//        );
//    }
//    @GetMapping("/type/{reportType}")
//    public ApiResponse<List<FinancialReportSummaryDTO>> getReportsByType(
//            @PathVariable FinancialReport.ReportType reportType
//    ) {
//        return ApiResponse.ok(
//                "Rapports financiers récupérés par type.",
//                financialReportService.getReportsByType(reportType)
//        );
//    }
//
//    @GetMapping("/period")
//    public ApiResponse<List<FinancialReportSummaryDTO>> getReportsByPeriod(
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
//    ) {
//        return ApiResponse.ok(
//                "Rapports financiers récupérés par période.",
//                financialReportService.getReportsByPeriod(startDate, endDate)
//        );
//    }
//    @PutMapping("/{id}")
//    public ApiResponse<FinancialReportResponseDTO> updateReport(
//            @PathVariable Long id,
//            @Valid @RequestBody FinancialReportUpdateDTO request
//    ) {
//        return ApiResponse.ok(
//                "Rapport financier mis à jour avec succès.",
//                financialReportService.updateReport(id, request)
//        );
//    }
//    @DeleteMapping("/{id}")
//    public ApiResponse<Void> deleteReport(@PathVariable Long id) {
//        financialReportService.deleteReport(id);
//        return ApiResponse.okMessage("Rapport financier supprimé avec succès.");
//    }
//}

package com.recyclix.backend.controller.accountant;

import com.recyclix.backend.dto.financial_report.FinancialReportRequestDTO;
import com.recyclix.backend.dto.financial_report.FinancialReportResponseDTO;
import com.recyclix.backend.dto.financial_report.FinancialReportSummaryDTO;
import com.recyclix.backend.dto.financial_report.FinancialReportUpdateDTO;
import com.recyclix.backend.model.FinancialReport;
import com.recyclix.backend.service.accountant.FinancialReportPdfService;
import com.recyclix.backend.service.accountant.FinancialReportService;
import com.recyclix.backend.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/accountant/reports")
@PreAuthorize("@factoryAccess.hasPosition('ACCOUNTANT')")
@RequiredArgsConstructor
public class FinancialReportController {

    private final FinancialReportService financialReportService;
    private final FinancialReportPdfService financialReportPdfService;

    // =========================================================
    // ENDPOINTS JSON (inchangés)
    // =========================================================

    @PostMapping("/generate")
    public ApiResponse<FinancialReportResponseDTO> generateAndSaveReport(
            @Valid @RequestBody FinancialReportRequestDTO request
    ) {
        return ApiResponse.ok(
                "Rapport financier généré et sauvegardé avec succès.",
                financialReportService.generateAndSaveReport(request)
        );
    }

    @GetMapping("/preview")
    public ApiResponse<FinancialReportResponseDTO> previewReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ApiResponse.ok(
                "Aperçu du rapport financier généré avec succès.",
                financialReportService.previewReport(startDate, endDate)
        );
    }

//    @GetMapping
//    public ApiResponse<List<FinancialReportSummaryDTO>> getAllReports() {
//        return ApiResponse.ok(
//                "Liste des rapports financiers récupérée.",
//                financialReportService.getAllReports()
//        );
//    }

    @GetMapping("/{id}")
    public ApiResponse<FinancialReportResponseDTO> getReportById(@PathVariable Long id) {
        return ApiResponse.ok(
                "Rapport financier récupéré avec succès.",
                financialReportService.getReportById(id)
        );
    }

    @GetMapping("/type/{reportType}")
    public ApiResponse<List<FinancialReportSummaryDTO>> getReportsByType(
            @PathVariable FinancialReport.ReportType reportType
    ) {
        return ApiResponse.ok(
                "Rapports financiers récupérés par type.",
                financialReportService.getReportsByType(reportType)
        );
    }

    @GetMapping("/period")
    public ApiResponse<List<FinancialReportSummaryDTO>> getReportsByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ApiResponse.ok(
                "Rapports financiers récupérés par période.",
                financialReportService.getReportsByPeriod(startDate, endDate)
        );
    }

    @PutMapping("/{id}")
    public ApiResponse<FinancialReportResponseDTO> updateReport(
            @PathVariable Long id,
            @Valid @RequestBody FinancialReportUpdateDTO request
    ) {
        return ApiResponse.ok(
                "Rapport financier mis à jour avec succès.",
                financialReportService.updateReport(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteReport(@PathVariable Long id) {
        financialReportService.deleteReport(id);
        return ApiResponse.okMessage("Rapport financier supprimé avec succès.");
    }

    // =========================================================
    // 🔥 NOUVEAUX ENDPOINTS PDF (avec ordre CORRIGÉ)
    // ⚠️ ATTENTION : Les endpoints fixes DOIVENT être avant {id}
    // =========================================================

    /**
     * Preview PDF (sans sauvegarde) - ENDPOINT FIXE avant {id}
     * GET /api/accountant/reports/preview/pdf
     */
    @GetMapping("/preview/pdf")
    public ResponseEntity<Resource> downloadPreviewPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return financialReportPdfService.generatePreviewPdf(startDate, endDate);
    }

    /**
     * Génère, sauvegarde ET télécharge un PDF - ENDPOINT FIXE avant {id}
     * POST /api/accountant/reports/generate/pdf
     */
    @PostMapping("/generate/pdf")
    public ResponseEntity<Resource> generateAndDownloadPdf(
            @RequestParam FinancialReport.ReportType reportType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return financialReportPdfService.generateAndDownloadReport(reportType, startDate, endDate);
    }

    /**
     * Télécharge PDF d'un rapport existant - ENDPOINT AVEC ID (doit être APRÈS les fixes)
     * GET /api/accountant/reports/{id}/pdf
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<Resource> downloadReportPdf(@PathVariable Long id) {
        return financialReportPdfService.generatePdfFromExistingReport(id);
    }
}