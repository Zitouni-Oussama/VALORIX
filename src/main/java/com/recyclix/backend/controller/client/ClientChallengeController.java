package com.recyclix.backend.controller.client;

import com.recyclix.backend.dto.challenge.ChallengeSummaryDTO;
import com.recyclix.backend.dto.challenge.TicketRequestChallengeResponse;
import com.recyclix.backend.dto.user_challenge.UserChallengeResponseDTO;
import com.recyclix.backend.dto.user_challenge.UserChallengeSummaryDTO;
import com.recyclix.backend.service.client.ClientChallengeService;
import com.recyclix.backend.util.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client/challenges")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
@Validated
public class ClientChallengeController {

    private final ClientChallengeService clientChallengeService;

    @GetMapping("/available")
    public ApiResponse<List<ChallengeSummaryDTO>> getAvailableChallenges() {
        return ApiResponse.ok(
                "Défis disponibles récupérés avec succès.",
                clientChallengeService.getAvailableChallenges()
        );
    }

    @PostMapping("/join")
    public ApiResponse<UserChallengeResponseDTO> joinChallenge(
            @Valid @RequestBody JoinChallengeRequest request
    ) {
        return ApiResponse.ok(
                "Défi rejoint avec succès.",
                clientChallengeService.joinChallenge(request.getChallengeId())
        );
    }

    @GetMapping("/my")
    public ApiResponse<List<UserChallengeSummaryDTO>> getMyChallenges() {
        return ApiResponse.ok(
                "Mes défis récupérés avec succès.",
                clientChallengeService.getMyChallenges()
        );
    }

    @GetMapping("/my/completed")
    public ApiResponse<List<UserChallengeSummaryDTO>> getMyCompletedChallenges() {
        return ApiResponse.ok(
                "Mes défis terminés récupérés avec succès.",
                clientChallengeService.getMyCompletedChallenges()
        );
    }

    @GetMapping("/my/{userChallengeId}")
    public ApiResponse<UserChallengeResponseDTO> getMyChallengeProgress(
            @PathVariable Long userChallengeId
    ) {
        return ApiResponse.ok(
                "Progression du défi récupérée avec succès.",
                clientChallengeService.getMyChallengeProgress(userChallengeId)
        );
    }

    @GetMapping("/my/rewards")
    public ApiResponse<ClientChallengeService.RewardOverviewResponse> getMyRewardsOverview() {
        return ApiResponse.ok(
                "Récompenses récupérées avec succès.",
                clientChallengeService.getMyRewardsOverview()
        );
    }

    @PostMapping("/my/{userChallengeId}/request-validation")
    public ApiResponse<TicketRequestChallengeResponse> requestChallengeValidation(
            @PathVariable Long userChallengeId
    ) {
        return ApiResponse.ok(
                "Demande de validation envoyée avec succès.",
                clientChallengeService.requestChallengeValidation(userChallengeId)
        );
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JoinChallengeRequest {
        @NotNull(message = "L'identifiant du défi est obligatoire.")
        private Long challengeId;
    }
}