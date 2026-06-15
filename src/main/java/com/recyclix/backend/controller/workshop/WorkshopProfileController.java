package com.recyclix.backend.controller.workshop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recyclix.backend.dto.account.AccountUpdateDTO;
import com.recyclix.backend.dto.factory_user.FactoryUserUpdateDTO;
import com.recyclix.backend.service.workshop.WorkshopProfileService;
import com.recyclix.backend.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/workshop/profile")
@RequiredArgsConstructor
@PreAuthorize("@factoryAccess.hasPosition('MANAGER')")
public class WorkshopProfileController {

    private final WorkshopProfileService workshopProfileService;
    private final ObjectMapper objectMapper;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<WorkshopProfileService.WorkshopProfileResponse>> getMyProfile() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Profil du chef d’atelier récupéré avec succès.",
                        workshopProfileService.getMyProfile()
                )
        );
    }

    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<WorkshopProfileService.WorkshopProfileResponse>> updateMyProfile(
            @RequestPart(value = "account", required = false) String accountJson,
            @RequestPart(value = "factoryUser", required = false) String factoryUserJson,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) throws Exception {

        AccountUpdateDTO accountDto = null;
        FactoryUserUpdateDTO factoryUserDto = null;

        if (accountJson != null && !accountJson.isBlank()) {
            accountDto = objectMapper.readValue(accountJson, AccountUpdateDTO.class);
        }

        if (factoryUserJson != null && !factoryUserJson.isBlank()) {
            factoryUserDto = objectMapper.readValue(factoryUserJson, FactoryUserUpdateDTO.class);
        }

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Profil du chef d’atelier mis à jour avec succès.",
                        workshopProfileService.updateMyProfile(accountDto, factoryUserDto, profileImage)
                )
        );
    }

    @DeleteMapping("/me/profile-image")
    public ResponseEntity<ApiResponse<WorkshopProfileService.WorkshopProfileResponse>> removeMyProfileImage() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Photo de profil supprimée avec succès.",
                        workshopProfileService.removeMyProfileImage()
                )
        );
    }
}