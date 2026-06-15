package com.recyclix.backend.controller.admin;

import com.recyclix.backend.dto.challenge.ChallengeRequestDTO;
import com.recyclix.backend.dto.challenge.ChallengeResponseDTO;
import com.recyclix.backend.dto.challenge.ChallengeSummaryDTO;
import com.recyclix.backend.dto.challenge.ChallengeUpdateDTO;
import com.recyclix.backend.service.admin.AdminChallengeService;
import com.recyclix.backend.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/challenges")
@RequiredArgsConstructor
@PreAuthorize("@factoryAccess.hasPosition('ADMIN')")
public class AdminChallengeController {

    private final AdminChallengeService adminChallengeService;

    @PostMapping
    public ApiResponse<ChallengeResponseDTO> createChallenge(@Valid @RequestBody ChallengeRequestDTO request) {
        return ApiResponse.ok("Défi créé avec succès.", adminChallengeService.createChallenge(request));
    }

    @GetMapping
    public ApiResponse<Page<ChallengeSummaryDTO>> getAllChallenges(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok("Défis récupérés avec succès.", adminChallengeService.getAllChallenges(page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<ChallengeResponseDTO> getChallengeById(@PathVariable Long id) {
        return ApiResponse.ok("Défi récupéré avec succès.", adminChallengeService.getChallengeById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<ChallengeResponseDTO> updateChallenge(
            @PathVariable Long id,
            @Valid @RequestBody ChallengeUpdateDTO request
    ) {
        return ApiResponse.ok("Défi mis à jour avec succès.", adminChallengeService.updateChallenge(id, request));
    }

    @PutMapping("/{id}/activate")
    public ApiResponse<ChallengeResponseDTO> activateChallenge(@PathVariable Long id) {
        return ApiResponse.ok("Défi activé avec succès.", adminChallengeService.activateChallenge(id));
    }

    @PutMapping("/{id}/deactivate")
    public ApiResponse<ChallengeResponseDTO> deactivateChallenge(@PathVariable Long id) {
        return ApiResponse.ok("Défi désactivé avec succès.", adminChallengeService.deactivateChallenge(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteChallenge(@PathVariable Long id) {
        adminChallengeService.deleteChallenge(id);
        return ApiResponse.okMessage("Défi supprimé avec succès.");
    }
}