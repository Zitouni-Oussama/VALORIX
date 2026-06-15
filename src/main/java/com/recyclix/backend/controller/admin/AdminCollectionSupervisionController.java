// recyclix\backend\controller\admin\AdminCollectionSupervisionController.java
package com.recyclix.backend.controller.admin;

import com.recyclix.backend.controller.client.ClientCollectionController;
import com.recyclix.backend.dto.admin.*;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.model.CollectionRequest;
import com.recyclix.backend.model.CollectionRequest.Status;
import com.recyclix.backend.model.FactoryDelivery;
import com.recyclix.backend.repository.CollectionRequestRepository;
import com.recyclix.backend.service.admin.AdminCollectionSupervisionService;
import com.recyclix.backend.service.qrcode.QRCodeService;
import com.recyclix.backend.util.ApiResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/supervision")
@RequiredArgsConstructor
@PreAuthorize("hasRole('FACTORY_USER') and @factoryAccess.hasPosition('ADMIN')")
public class AdminCollectionSupervisionController {

    private final AdminCollectionSupervisionService supervisionService;

    // ----------------------------------------
    // 1. STATISTIQUES DE SUPERVISION
    // ----------------------------------------
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<SupervisionStatsDTO>> getSupervisionStats() {
        SupervisionStatsDTO stats = supervisionService.getSupervisionStats();
        return ResponseEntity.ok(
                ApiResponse.ok("Statistiques de supervision récupérées avec succès.", stats)
        );
    }

    // ----------------------------------------
    // 2. LISTE DES DEMANDES (FILTRES + PAGINATION)
    // ----------------------------------------
    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<Page<CollectionRequestSupervisionDTO>>> getAllRequests(
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long collectorId,
            @RequestParam(required = false) Long materialId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<CollectionRequestSupervisionDTO> requests = supervisionService.getAllRequests(
                status, clientId, collectorId, materialId, startDate, endDate, page, size
        );

        return ResponseEntity.ok(
                ApiResponse.ok("Demandes de collecte récupérées avec succès.", requests)
        );
    }

    // ----------------------------------------
    // 3. DÉTAIL COMPLET D'UNE DEMANDE
    // ----------------------------------------
    @GetMapping("/requests/{requestId}")
    public ResponseEntity<ApiResponse<RequestFullDetailDTO>> getRequestDetail(
            @PathVariable Long requestId
    ) {
        RequestFullDetailDTO detail = supervisionService.getRequestDetail(requestId);
        return ResponseEntity.ok(
                ApiResponse.ok("Détail complet de la demande récupéré avec succès.", detail)
        );
    }

    // ----------------------------------------
    // 4. DÉTECTION DE LITIGES
    // ----------------------------------------
    @GetMapping("/disputes")
    public ResponseEntity<ApiResponse<Page<DisputedRequestDTO>>> getDisputedRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<DisputedRequestDTO> disputes = supervisionService.getDisputedRequests(page, size);
        return ResponseEntity.ok(
                ApiResponse.ok("Demandes litigieuses récupérées avec succès.", disputes)
        );
    }

    // ----------------------------------------
    // 5. ANNULATION FORCÉE PAR L'ADMIN
    // ----------------------------------------
    @PutMapping("/requests/{requestId}/force-cancel")
    public ResponseEntity<ApiResponse<RequestFullDetailDTO>> forceCancelRequest(
            @PathVariable Long requestId,
            @RequestBody ForceCancelRequest body
    ) {
        RequestFullDetailDTO detail = supervisionService.forceCancelRequest(requestId, body.getReason());
        return ResponseEntity.ok(
                ApiResponse.ok("Demande annulée avec succès par l'administrateur.", detail)
        );
    }

    // ----------------------------------------
    // 6. RÉASSIGNATION D'UNE DEMANDE (OPTIONNEL AVANCÉ)
    // ----------------------------------------
    @PutMapping("/requests/{requestId}/reassign")
    public ResponseEntity<ApiResponse<RequestFullDetailDTO>> reassignRequest(
            @PathVariable Long requestId,
            @RequestBody ReassignRequest body
    ) {
        RequestFullDetailDTO detail = supervisionService.reassignRequest(requestId, body.getCollectorId());
        return ResponseEntity.ok(
                ApiResponse.ok("Demande réassignée avec succès.", detail)
        );
    }

    // ============================================================
    // DTOs INTERNES POUR LES REQUÊTES
    // ============================================================

    @Data
    public static class ForceCancelRequest {
        private String reason;
    }

    @Data
    public static class ReassignRequest {
        private Long collectorId;
    }

}