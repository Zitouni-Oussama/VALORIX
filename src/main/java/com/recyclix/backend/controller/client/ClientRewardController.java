package com.recyclix.backend.controller.client;

import com.recyclix.backend.service.client.ClientRewardService;
import com.recyclix.backend.util.ApiResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/client/rewards")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
public class ClientRewardController {

    private final ClientRewardService clientRewardService;

    //. -------------------- CATALOGUE -------------------- .\\

    /**
     * Voir toutes les récompenses disponibles
     * GET /api/client/rewards
     */
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAvailableRewards() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Catalogue des récompenses récupéré avec succès.",
                        clientRewardService.getAvailableRewards()
                )
        );
    }

    /**
     * Détail d'une récompense
     * GET /api/client/rewards/{rewardId}
     */
    @GetMapping("/{rewardId}")
    public ResponseEntity<ApiResponse<?>> getRewardDetail(@PathVariable Long rewardId) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Détail de la récompense récupéré avec succès.",
                        clientRewardService.getRewardDetail(rewardId)
                )
        );
    }

    /**
     * Filtrer par catégorie
     * GET /api/client/rewards/category/RECHARGE_PHONE
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<?>> getRewardsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Récompenses filtrées par catégorie.",
                        clientRewardService.getRewardsByCategory(category)
                )
        );
    }

    //. -------------------- ÉCHANGE -------------------- .\\

    /**
     * Échanger des points contre une récompense
     * POST /api/client/rewards/{rewardId}/redeem
     */
    @PostMapping("/{rewardId}/redeem")
    public ResponseEntity<ApiResponse<?>> redeemReward(
            @PathVariable Long rewardId,
            @RequestBody(required = false) RedeemRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Échange effectué avec succès ! En attente de validation.",
                        clientRewardService.redeemReward(rewardId, request)
                )
        );
    }

    /**
     * Voir l'historique de mes échanges
     * GET /api/client/rewards/my-redemptions
     */
    @GetMapping("/my-redemptions")
    public ResponseEntity<ApiResponse<?>> getMyRedemptions() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Historique des échanges récupéré.",
                        clientRewardService.getMyRedemptions()
                )
        );
    }

    // =========================================================
    // DTO INTERNE
    // =========================================================

    @Data
    public static class RedeemRequest {
        private String additionalInfo;  // ex: numéro de téléphone pour recharge
    }
}