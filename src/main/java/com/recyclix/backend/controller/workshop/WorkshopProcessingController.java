package com.recyclix.backend.controller.workshop;

import com.recyclix.backend.dto.workshop.*;
import com.recyclix.backend.service.workshop.TreatmentService;
import com.recyclix.backend.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workshop/processing")
@RequiredArgsConstructor
@PreAuthorize("@factoryAccess.hasPosition('MANAGER')")
public class WorkshopProcessingController {

    private final TreatmentService treatmentService;

    // Stock
    @GetMapping("/stock")
    public ResponseEntity<ApiResponse<List<MaterialStockDTO>>> getMaterialStock() {
        return ResponseEntity.ok(ApiResponse.ok("Stock récupéré", treatmentService.getAllMaterialStocks()));
    }

    // Traitement
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<TreatmentBatchDTO>> startTreatment(@Valid @RequestBody StartTreatmentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Lot de traitement démarré", treatmentService.startTreatment(request)));
    }

    @PutMapping("/{batchId}/complete")
    public ResponseEntity<ApiResponse<TreatmentBatchDTO>> completeTreatment(
            @PathVariable Long batchId,
            @Valid @RequestBody CompleteTreatmentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Lot terminé", treatmentService.completeTreatment(batchId, request)));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<TreatmentBatchDTO>>> getProcessingHistory(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long materialId) {
        return ResponseEntity.ok(ApiResponse.ok("Historique récupéré", treatmentService.getProcessingHistory(status, materialId)));
    }

    @PutMapping("/{batchId}")
    public ResponseEntity<ApiResponse<TreatmentBatchDTO>> updateTreatmentBatch(
            @PathVariable Long batchId,
            @Valid @RequestBody UpdateTreatmentBatchRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Lot mis à jour", treatmentService.updateTreatmentBatch(batchId, request)));
    }

    @DeleteMapping("/{batchId}")
    public ResponseEntity<ApiResponse<Void>> deleteTreatmentBatch(@PathVariable Long batchId) {
        treatmentService.deleteTreatmentBatch(batchId);
        return ResponseEntity.ok(ApiResponse.okMessage("Lot supprimé"));
    }
}