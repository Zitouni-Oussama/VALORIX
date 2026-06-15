package com.recyclix.backend.controller.accountant;

import com.recyclix.backend.service.accountant.AccountantRewardService;
import com.recyclix.backend.util.ApiResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/accountant/rewards")
@RequiredArgsConstructor
@PreAuthorize("@factoryAccess.hasPosition('ACCOUNTANT')")
public class AccountantRewardController {

    private final AccountantRewardService accountantRewardService;

    // =========================================================
    // CRÉER UNE RÉCOMPENSE (NOUVEAU)
    // =========================================================

    /**
     * Créer une nouvelle récompense dans le catalogue
     * POST /api/accountant/rewards
     */
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createReward(
            @Valid @RequestBody CreateRewardRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Récompense créée avec succès.",
                        accountantRewardService.createReward(request)
                )
        );
    }

    /**
     * Mettre à jour une récompense existante
     * PUT /api/accountant/rewards/{rewardId}
     */
    @PutMapping("/{rewardId}")
    public ResponseEntity<ApiResponse<?>> updateReward(
            @PathVariable Long rewardId,
            @Valid @RequestBody UpdateRewardRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Récompense mise à jour avec succès.",
                        accountantRewardService.updateReward(rewardId, request)
                )
        );
    }

    /**
     * Activer / Désactiver une récompense
     * PUT /api/accountant/rewards/{rewardId}/toggle
     */
    @PutMapping("/{rewardId}/toggle")
    public ResponseEntity<ApiResponse<?>> toggleReward(@PathVariable Long rewardId) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Statut de la récompense modifié avec succès.",
                        accountantRewardService.toggleReward(rewardId)
                )
        );
    }

    /**
     * Supprimer une récompense
     * DELETE /api/accountant/rewards/{rewardId}
     */
    @DeleteMapping("/{rewardId}")
    public ResponseEntity<ApiResponse<Void>> deleteReward(@PathVariable Long rewardId) {
        accountantRewardService.deleteReward(rewardId);
        return ResponseEntity.ok(
                ApiResponse.okMessage("Récompense supprimée avec succès.")
        );
    }

    /**
     * Voir toutes les récompenses (actives et inactives)
     * GET /api/accountant/rewards/all
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<?>> getAllRewards() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Toutes les récompenses récupérées.",
                        accountantRewardService.getAllRewards()
                )
        );
    }

    // =========================================================
    // ENDPOINTS EXISTANTS (GESTION DES DEMANDES)
    // =========================================================

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<?>> getPendingRedemptions() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Demandes en attente récupérées.",
                        accountantRewardService.getPendingRedemptions()
                )
        );
    }

    @PutMapping("/{redemptionId}/approve")
    public ResponseEntity<ApiResponse<?>> approveRedemption(
            @PathVariable Long redemptionId,
            @RequestBody(required = false) ReviewRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Échange approuvé avec succès.",
                        accountantRewardService.approveRedemption(redemptionId, request)
                )
        );
    }

    @PutMapping("/{redemptionId}/reject")
    public ResponseEntity<ApiResponse<?>> rejectRedemption(
            @PathVariable Long redemptionId,
            @RequestBody ReviewRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Échange rejeté.",
                        accountantRewardService.rejectRedemption(redemptionId, request)
                )
        );
    }

//    @PutMapping("/{redemptionId}/deliver")
//    public ResponseEntity<ApiResponse<?>> markAsDelivered(@PathVariable Long redemptionId) {
//        return ResponseEntity.ok(
//                ApiResponse.ok(
//                        "Marqué comme livré avec succès.",
//                        accountantRewardService.markAsDelivered(redemptionId)
//                )
//        );
//    }

    // =========================================================
    // DTOs
    // =========================================================

    @Data
    public static class CreateRewardRequest {
        @NotBlank(message = "Le nom de la récompense est obligatoire.")
        @Size(max = 150)
        private String name;

        @Size(max = 500)
        private String description;

        @NotNull(message = "Le coût en points est obligatoire.")
        @Min(value = 1, message = "Le coût doit être d'au moins 1 point.")
        private Integer pointsCost;

        private BigDecimal monetaryValue;

        @NotBlank(message = "La catégorie est obligatoire.")
        private String category;

        private String imageUrl;

        private Integer stock;  // -1 = illimité, sinon nombre

        private Boolean isActive;

        @Size(max = 100)
        private String partnerName;

        private Integer validityDays;

        @Size(max = 500)
        private String deliveryInstructions;
    }

    @Data
    public static class UpdateRewardRequest {
        private String name;
        private String description;
        private Integer pointsCost;
        private BigDecimal monetaryValue;
        private String category;
        private String imageUrl;
        private Integer stock;
        private Boolean isActive;
        private String partnerName;
        private Integer validityDays;
        private String deliveryInstructions;
    }

    @Data
    public static class ReviewRequest {
        private String note;
    }
}