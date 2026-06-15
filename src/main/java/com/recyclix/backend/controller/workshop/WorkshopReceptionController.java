package com.recyclix.backend.controller.workshop;

import com.recyclix.backend.dto.workshop.CollectionFullDetailsDTO;
import com.recyclix.backend.service.workshop.WorkshopReceptionService;
import com.recyclix.backend.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workshop/receptions")
@RequiredArgsConstructor
@PreAuthorize("@factoryAccess.hasPosition('MANAGER')")
public class WorkshopReceptionController {

    private final WorkshopReceptionService workshopReceptionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkshopReceptionService.WorkshopReceptionSummaryResponse>>> getAllDeliveries() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Livraisons récupérées avec succès.",
                        workshopReceptionService.getAllDeliveries()
                )
        );
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<WorkshopReceptionService.WorkshopReceptionSummaryResponse>>> getPendingDeliveries() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Livraisons en attente récupérées avec succès.",
                        workshopReceptionService.getPendingDeliveries()
                )
        );
    }

    @GetMapping("/{deliveryId}")
    public ResponseEntity<ApiResponse<WorkshopReceptionService.WorkshopReceptionDetailResponse>> getDeliveryById(
            @PathVariable Long deliveryId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Détail de la livraison récupéré avec succès.",
                        workshopReceptionService.getDeliveryById(deliveryId)
                )
        );
    }

    @PostMapping("/collections/{collectionId}/receive")
    public ResponseEntity<ApiResponse<WorkshopReceptionService.WorkshopReceptionDetailResponse>> receiveCollection(
            @PathVariable Long collectionId,
            @Valid @RequestBody WorkshopReceptionService.ReceiveCollectionRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Réception de la collecte enregistrée avec succès.",
                        workshopReceptionService.receiveCollection(collectionId, request)
                )
        );
    }

    @PutMapping("/{deliveryId}/validate")
    public ResponseEntity<ApiResponse<WorkshopReceptionService.WorkshopReceptionDetailResponse>> validateDelivery(
            @PathVariable Long deliveryId,
            @Valid @RequestBody WorkshopReceptionService.ValidateDeliveryRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Livraison validée avec succès.",
                        workshopReceptionService.validateDelivery(deliveryId, request)
                )
        );
    }

    @PutMapping("/{deliveryId}/reject")
    public ResponseEntity<ApiResponse<WorkshopReceptionService.WorkshopReceptionDetailResponse>> rejectDelivery(
            @PathVariable Long deliveryId,
            @Valid @RequestBody WorkshopReceptionService.RejectDeliveryRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Livraison rejetée avec succès.",
                        workshopReceptionService.rejectDelivery(deliveryId, request)
                )
        );
    }

    // Dans WorkshopReceptionController.java

    @GetMapping("/collections/{collectionId}/full-details")
    public ResponseEntity<ApiResponse<CollectionFullDetailsDTO>> getCollectionFullDetails(
            @PathVariable Long collectionId) {
        CollectionFullDetailsDTO details = workshopReceptionService.getCollectionFullDetails(collectionId);
        return ResponseEntity.ok(ApiResponse.ok("Détails complets de la collecte", details));
    }
}