package com.recyclix.backend.controller.accountant;

import com.recyclix.backend.dto.factory_invoice.FactoryInvoiceRequestDTO;
import com.recyclix.backend.dto.factory_invoice.FactoryInvoiceResponseDTO;
import com.recyclix.backend.model.FactoryInvoice;
import com.recyclix.backend.service.accountant.FactoryInvoicePdfService;
import com.recyclix.backend.service.accountant.FactoryInvoiceService;
import com.recyclix.backend.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/accountant/factory-invoices")
@PreAuthorize("@factoryAccess.hasPosition('ACCOUNTANT')")
@RequiredArgsConstructor
public class FactoryInvoiceController {

    private final FactoryInvoiceService invoiceService;
    private final FactoryInvoicePdfService pdfService;

    // =========================================================
    // CRUD
    // =========================================================

    @PostMapping
    public ResponseEntity<ApiResponse<FactoryInvoiceResponseDTO>> createInvoice(
            @Valid @RequestBody FactoryInvoiceRequestDTO request
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok("Facture créée avec succès.", invoiceService.createInvoice(request))
        );
    }

//    @GetMapping
//    public ResponseEntity<ApiResponse<List<FactoryInvoiceResponseDTO>>> getAllInvoices() {
//        return ResponseEntity.ok(
//                ApiResponse.ok("Liste des factures récupérée.", invoiceService.getAllInvoices())
//        );
//    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FactoryInvoiceResponseDTO>> getInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.ok("Facture récupérée avec succès.", invoiceService.getInvoiceById(id))
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FactoryInvoiceResponseDTO>> updateInvoice(
            @PathVariable Long id,
            @Valid @RequestBody FactoryInvoiceRequestDTO request
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok("Facture mise à jour avec succès.", invoiceService.updateInvoice(id, request))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.ok(ApiResponse.okMessage("Facture supprimée avec succès."));
    }

    // =========================================================
    // WORKFLOW
    // =========================================================

    @PutMapping("/{id}/pay")
    public ResponseEntity<ApiResponse<FactoryInvoiceResponseDTO>> markAsPaid(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.ok("Facture marquée comme payée.", invoiceService.markAsPaid(id))
        );
    }

    @PutMapping("/{id}/overdue")
    public ResponseEntity<ApiResponse<FactoryInvoiceResponseDTO>> markAsOverdue(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.ok("Facture marquée comme en retard.", invoiceService.markAsOverdue(id))
        );
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<FactoryInvoiceResponseDTO>> cancelInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.ok("Facture annulée avec succès.", invoiceService.cancelInvoice(id))
        );
    }

    // =========================================================
    // FILTRES
    // =========================================================

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<FactoryInvoiceResponseDTO>>> getInvoicesByStatus(
            @PathVariable FactoryInvoice.InvoiceStatus status
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok("Factures filtrées par statut.", invoiceService.getInvoicesByStatus(status))
        );
    }

    // =========================================================
    // STATISTIQUES
    // =========================================================

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<FactoryInvoiceService.InvoiceStatistics>> getStatistics() {
        return ResponseEntity.ok(
                ApiResponse.ok("Statistiques récupérées.", invoiceService.getStatistics())
        );
    }

    // =========================================================
    // PDF
    // =========================================================

    @GetMapping("/{id}/pdf")
    public ResponseEntity<Resource> downloadInvoicePdf(@PathVariable Long id) {
        return pdfService.generateFactoryInvoicePdf(id);
    }

//    @PostMapping("/preview/pdf")
//    public ResponseEntity<Resource> previewInvoicePdf(@RequestBody InvoicePreviewRequest request) {
//        return pdfService.previewFactoryInvoicePdf(
//                request.getFactoryName(),
//                request.getAmountHt(),
//                request.getTvaRate(),
//                request.getDueDate(),
//                request.getStatus()
//        );
//    }

    @PostMapping("/preview/pdf")
    public ResponseEntity<Resource> previewInvoicePdf(@RequestBody InvoicePreviewRequest request) {
        return pdfService.previewFactoryInvoicePdf(
                request.getFactoryName(),
                request.getAmountHt(),      // Changé: amount -> amountHt
                request.getTvaRate(),       // NOUVEAU paramètre
                request.getDueDate(),
                request.getStatus()
        );
    }

    // DTO corrigé
    @Data
    public static class InvoicePreviewRequest {
        private String factoryName;
        private BigDecimal amountHt;      // Changé: amount -> amountHt
        private BigDecimal tvaRate;       // NOUVEAU
        private LocalDate dueDate;
        private FactoryInvoice.InvoiceStatus status;
    }

}