package com.recyclix.backend.controller.client;

import com.recyclix.backend.dto.material.ClientMaterialDetailDTO;
import com.recyclix.backend.dto.material.ClientMaterialSummaryDTO;
import com.recyclix.backend.service.client.ClientMaterialService;
import com.recyclix.backend.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client/materials")
@RequiredArgsConstructor
//@PreAuthorize("hasRole('CLIENT')")
@PreAuthorize("hasAnyRole('CLIENT', 'COLLECTOR')")
public class ClientMaterialController {

    private final ClientMaterialService clientMaterialService;

    @GetMapping
    public ApiResponse<List<ClientMaterialSummaryDTO>> getAvailableMaterials() {
        return ApiResponse.ok(
                "Matériaux récupérés avec succès.",
                clientMaterialService.getAvailableMaterials()
        );
    }

    @GetMapping("/{materialId}")
    public ApiResponse<ClientMaterialDetailDTO> getMaterialDetails(@PathVariable Long materialId) {
        return ApiResponse.ok(
                "Détails du matériau récupérés avec succès.",
                clientMaterialService.getMaterialDetails(materialId)
        );
    }
}