package com.recyclix.backend.controller.admin;

import com.recyclix.backend.dto.collector.CollectorResponseDTO;
import com.recyclix.backend.dto.collector.CollectorSummaryDTO;
import com.recyclix.backend.dto.truck.TruckResponseDTO;
import com.recyclix.backend.service.admin.AdminCollectorVerificationService;
import com.recyclix.backend.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/collectors")
@RequiredArgsConstructor
@PreAuthorize("@factoryAccess.hasPosition('ADMIN')")
public class AdminCollectorVerificationController {

    private final AdminCollectorVerificationService adminCollectorVerificationService;

    @GetMapping
    public ApiResponse<Page<CollectorSummaryDTO>> getAllCollectors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(
                "Liste des collecteurs récupérée avec succès.",
                adminCollectorVerificationService.getAllCollectors(page, size)
        );
    }

    @GetMapping("/verified")
    public ApiResponse<Page<CollectorSummaryDTO>> getVerifiedCollectors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(
                "Collecteurs vérifiés récupérés avec succès.",
                adminCollectorVerificationService.getVerifiedCollectors(page, size)
        );
    }

    @GetMapping("/unverified")
    public ApiResponse<Page<CollectorSummaryDTO>> getUnverifiedCollectors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(
                "Collecteurs non vérifiés récupérés avec succès.",
                adminCollectorVerificationService.getUnverifiedCollectors(page, size)
        );
    }

    @GetMapping("/{collectorId}/verification")
    public ApiResponse<AdminCollectorVerificationService.CollectorVerificationDetailResponse> getCollectorVerificationDetails(
            @PathVariable Long collectorId
    ) {
        return ApiResponse.ok(
                "Détails de vérification du collecteur récupérés avec succès.",
                adminCollectorVerificationService.getCollectorVerificationDetails(collectorId)
        );
    }

    @GetMapping("/{collectorId}/truck")
    public ApiResponse<TruckResponseDTO> getCollectorTruck(@PathVariable Long collectorId) {
        return ApiResponse.ok(
                "Camion du collecteur récupéré avec succès.",
                adminCollectorVerificationService.getCollectorTruck(collectorId)
        );
    }

    @GetMapping("/verification/stats")
    public ApiResponse<AdminCollectorVerificationService.CollectorVerificationStatsResponse> getVerificationStats() {
        return ApiResponse.ok(
                "Statistiques de vérification récupérées avec succès.",
                adminCollectorVerificationService.getVerificationStats()
        );
    }

    @PutMapping("/{collectorId}/verify")
    public ApiResponse<CollectorResponseDTO> verifyCollector(@PathVariable Long collectorId) {
        return ApiResponse.ok(
                "Collecteur vérifié avec succès.",
                adminCollectorVerificationService.verifyCollector(collectorId)
        );
    }

    @PutMapping("/{collectorId}/reject")
    public ApiResponse<CollectorResponseDTO> rejectCollector(
            @PathVariable Long collectorId,
            @RequestBody(required = false) AdminCollectorVerificationService.RejectCollectorRequest request
    ) {
        return ApiResponse.ok(
                "Vérification du collecteur rejetée avec succès.",
                adminCollectorVerificationService.rejectCollector(collectorId, request)
        );
    }

    @PutMapping("/{collectorId}/unverify")
    public ApiResponse<CollectorResponseDTO> unverifyCollector(@PathVariable Long collectorId) {
        return ApiResponse.ok(
                "Collecteur marqué comme non vérifié avec succès.",
                adminCollectorVerificationService.unverifyCollector(collectorId)
        );
    }
}