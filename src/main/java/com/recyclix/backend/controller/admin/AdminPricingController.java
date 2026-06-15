package com.recyclix.backend.controller.admin;

import com.recyclix.backend.dto.material.MaterialResponseDTO;
import com.recyclix.backend.dto.material.MaterialSummaryDTO;
import com.recyclix.backend.service.admin.AdminPricingService;
import com.recyclix.backend.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/pricing")
@RequiredArgsConstructor
@PreAuthorize("@factoryAccess.hasPosition('ADMIN')")
public class AdminPricingController {

    private final AdminPricingService adminPricingService;

    @GetMapping("/materials")
    public ApiResponse<Page<MaterialSummaryDTO>> getAllMaterials(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(
                "Matériaux récupérés avec succès.",
                adminPricingService.getAllMaterials(page, size)
        );
    }

    @GetMapping("/materials/active")
    public ApiResponse<Page<MaterialSummaryDTO>> getActiveMaterials(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(
                "Matériaux actifs récupérés avec succès.",
                adminPricingService.getActiveMaterials(page, size)
        );
    }

    @GetMapping("/materials/inactive")
    public ApiResponse<Page<MaterialSummaryDTO>> getInactiveMaterials(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(
                "Matériaux inactifs récupérés avec succès.",
                adminPricingService.getInactiveMaterials(page, size)
        );
    }

    @GetMapping("/materials/{materialId}")
    public ApiResponse<MaterialResponseDTO> getMaterialById(@PathVariable Long materialId) {
        return ApiResponse.ok(
                "Matériau récupéré avec succès.",
                adminPricingService.getMaterialById(materialId)
        );
    }

    @PostMapping("/materials")
    public ApiResponse<MaterialResponseDTO> createMaterial(
            @RequestBody AdminPricingService.CreateMaterialRequest request
    ) {
        return ApiResponse.ok(
                "Matériau créé avec succès.",
                adminPricingService.createMaterial(request)
        );
    }

    @PutMapping("/materials/{materialId}")
    public ApiResponse<MaterialResponseDTO> updateMaterial(
            @PathVariable Long materialId,
            @RequestBody AdminPricingService.UpdateMaterialRequest request
    ) {
        return ApiResponse.ok(
                "Matériau modifié avec succès.",
                adminPricingService.updateMaterial(materialId, request)
        );
    }

    @PutMapping("/materials/{materialId}/price")
    public ApiResponse<MaterialResponseDTO> updateMaterialPrice(
            @PathVariable Long materialId,
            @RequestBody AdminPricingService.UpdateMaterialPriceRequest request
    ) {
        return ApiResponse.ok(
                "Prix du matériau modifié avec succès.",
                adminPricingService.updateMaterialPrice(materialId, request)
        );
    }

    @PutMapping("/materials/{materialId}/activate")
    public ApiResponse<MaterialResponseDTO> activateMaterial(@PathVariable Long materialId) {
        return ApiResponse.ok(
                "Matériau activé avec succès.",
                adminPricingService.activateMaterial(materialId)
        );
    }

    @PutMapping("/materials/{materialId}/deactivate")
    public ApiResponse<MaterialResponseDTO> deactivateMaterial(@PathVariable Long materialId) {
        return ApiResponse.ok(
                "Matériau désactivé avec succès.",
                adminPricingService.deactivateMaterial(materialId)
        );
    }

    @GetMapping("/stats")
    public ApiResponse<AdminPricingService.PricingStatsResponse> getPricingStats() {
        return ApiResponse.ok(
                "Statistiques des prix récupérées avec succès.",
                adminPricingService.getPricingStats()
        );
    }
}