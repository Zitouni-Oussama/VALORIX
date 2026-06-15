package com.recyclix.backend.controller.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recyclix.backend.dto.account.AccountUpdateDTO;
import com.recyclix.backend.dto.client.ClientUpdateDTO;
import com.recyclix.backend.service.client.ClientAccountService;
import com.recyclix.backend.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/client/account")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
public class ClientAccountController {

    private final ClientAccountService clientAccountService;
    private final ObjectMapper objectMapper;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ClientAccountService.ClientAccountProfileResponse>> getMyProfile() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Profil client récupéré avec succès.",
                        clientAccountService.getMyProfile()
                )
        );
    }

    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ClientAccountService.ClientAccountProfileResponse>> updateMyProfile(
            @RequestPart(value = "account", required = false) String accountJson,
            @RequestPart(value = "client", required = false) String clientJson,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) throws Exception {

        AccountUpdateDTO accountDto = null;
        ClientUpdateDTO clientDto = null;

        if (accountJson != null && !accountJson.isBlank()) {
            accountDto = objectMapper.readValue(accountJson, AccountUpdateDTO.class);
        }

        if (clientJson != null && !clientJson.isBlank()) {
            clientDto = objectMapper.readValue(clientJson, ClientUpdateDTO.class);
        }

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Profil client mis à jour avec succès.",
                        clientAccountService.updateMyProfile(accountDto, clientDto, profileImage)
                )
        );
    }

    @DeleteMapping("/me/profile-image")
    public ResponseEntity<ApiResponse<ClientAccountService.ClientAccountProfileResponse>> removeMyProfileImage() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Photo de profil supprimée avec succès.",
                        clientAccountService.removeMyProfileImage()
                )
        );
    }
}