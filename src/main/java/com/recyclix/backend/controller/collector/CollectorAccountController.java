package com.recyclix.backend.controller.collector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recyclix.backend.dto.account.AccountUpdateDTO;
import com.recyclix.backend.dto.collector.CollectorUpdateDTO;
import com.recyclix.backend.service.collector.CollectorAccountService;
import com.recyclix.backend.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/collector/account")
@RequiredArgsConstructor
@PreAuthorize("hasRole('COLLECTOR')")
public class CollectorAccountController {

    private final CollectorAccountService collectorAccountService;
    private final ObjectMapper objectMapper;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CollectorAccountService.CollectorAccountProfileResponse>> getMyProfile() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Profil collecteur récupéré avec succès.",
                        collectorAccountService.getMyProfile()
                )
        );
    }

    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CollectorAccountService.CollectorAccountProfileResponse>> updateMyProfile(
            @RequestPart(value = "account", required = false) String accountJson,
            @RequestPart(value = "collector", required = false) String collectorJson,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) throws Exception {

        AccountUpdateDTO accountDto = null;
        CollectorUpdateDTO collectorDto = null;

        if (accountJson != null && !accountJson.isBlank()) {
            accountDto = objectMapper.readValue(accountJson, AccountUpdateDTO.class);
        }

        if (collectorJson != null && !collectorJson.isBlank()) {
            collectorDto = objectMapper.readValue(collectorJson, CollectorUpdateDTO.class);
        }

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Profil collecteur mis à jour avec succès.",
                        collectorAccountService.updateMyProfile(accountDto, collectorDto, profileImage)
                )
        );
    }

    @DeleteMapping("/me/profile-image")
    public ResponseEntity<ApiResponse<CollectorAccountService.CollectorAccountProfileResponse>> removeMyProfileImage() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Photo de profil supprimée avec succès.",
                        collectorAccountService.removeMyProfileImage()
                )
        );
    }
}