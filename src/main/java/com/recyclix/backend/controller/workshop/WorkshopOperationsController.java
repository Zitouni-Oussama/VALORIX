package com.recyclix.backend.controller.workshop;

import com.recyclix.backend.service.workshop.WorkshopOperationsService;
import com.recyclix.backend.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workshop/operations")
@RequiredArgsConstructor
@PreAuthorize("@factoryAccess.hasPosition('MANAGER')")
public class WorkshopOperationsController {

    private final WorkshopOperationsService workshopOperationsService;

    @GetMapping("/processing")
    public ResponseEntity<ApiResponse<List<WorkshopOperationsService.ProcessingItemResponse>>> getProcessingItems() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Éléments en traitement récupérés avec succès.",
                        workshopOperationsService.getProcessingItems()
                )
        );
    }

    @GetMapping("/completed")
    public ResponseEntity<ApiResponse<List<WorkshopOperationsService.ProcessingItemResponse>>> getCompletedItems() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Éléments terminés récupérés avec succès.",
                        workshopOperationsService.getCompletedItems()
                )
        );
    }

    @PutMapping("/deliveries/{deliveryId}/start-processing")
    public ResponseEntity<ApiResponse<WorkshopOperationsService.ProcessingItemResponse>> startProcessing(
            @PathVariable Long deliveryId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Traitement démarré avec succès.",
                        workshopOperationsService.startProcessing(deliveryId)
                )
        );
    }

    @PutMapping("/deliveries/{deliveryId}/complete-processing")
    public ResponseEntity<ApiResponse<WorkshopOperationsService.ProcessingItemResponse>> completeProcessing(
            @PathVariable Long deliveryId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Traitement terminé avec succès.",
                        workshopOperationsService.completeProcessing(deliveryId)
                )
        );
    }

    @GetMapping("/stocks")
    public ResponseEntity<ApiResponse<WorkshopOperationsService.StockOverviewResponse>> getStockOverview() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Vue globale du stock récupérée avec succès.",
                        workshopOperationsService.getStockOverview()
                )
        );
    }

    @GetMapping("/stocks/materials")
    public ResponseEntity<ApiResponse<List<WorkshopOperationsService.MaterialStockResponse>>> getStockByMaterial() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Stock par matériau récupéré avec succès.",
                        workshopOperationsService.getStockByMaterial()
                )
        );
    }

    @GetMapping("/production/overview")
    public ResponseEntity<ApiResponse<WorkshopOperationsService.ProductionOverviewResponse>> getProductionOverview() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Vue globale de la production récupérée avec succès.",
                        workshopOperationsService.getProductionOverview()
                )
        );
    }
}