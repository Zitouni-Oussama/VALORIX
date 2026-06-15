package com.recyclix.backend.controller.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recyclix.backend.dto.ai_classification.AIClassificationResponseDTO;
import com.recyclix.backend.dto.collection.CollectionResponseDTO;
import com.recyclix.backend.dto.collection_request.*;
import com.recyclix.backend.dto.collector_location_history.CollectorLocationHistoryResponseDTO;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.model.CollectionRequest;
import com.recyclix.backend.service.client.ClientCollectionService;
import com.recyclix.backend.service.qrcode.QRCodeService;
import com.recyclix.backend.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api/client/collections")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
public class ClientCollectionController {

    private final ClientCollectionService clientCollectionService;
    private final ObjectMapper objectMapper;
    private final QRCodeService qrCodeService;

    @PostMapping(value = "/requests", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CollectionRequestResponseDTO>> createRequest(
            @RequestPart("request") String requestJson,
            @RequestPart(value = "wasteImage", required = false) MultipartFile wasteImage
    ) throws Exception {

        CollectionRequestRequestDTO request =
                objectMapper.readValue(requestJson, CollectionRequestRequestDTO.class);

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Demande de collecte créée avec succès.",
                        clientCollectionService.createRequest(request, wasteImage)
                )
        );
    }

    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<CollectionRequestSummaryDTO>>> getMyRequests() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Demandes de collecte récupérées avec succès.",
                        clientCollectionService.getMyRequests()
                )
        );
    }

    @GetMapping("/requests/{requestId}")
    public ResponseEntity<ApiResponse<CollectionRequestResponseDTO>> getMyRequestById(
            @PathVariable Long requestId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Demande de collecte récupérée avec succès.",
                        clientCollectionService.getMyRequestById(requestId)
                )
        );
    }

    @PutMapping("/requests/{requestId}/cancel")
    public ResponseEntity<ApiResponse<CollectionRequestResponseDTO>> cancelRequest(
            @PathVariable Long requestId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Demande de collecte annulée avec succès.",
                        clientCollectionService.cancelRequest(requestId)
                )
        );
    }

    @GetMapping("/requests/{requestId}/ai-result")
    public ResponseEntity<ApiResponse<AIClassificationResponseDTO>> getAiResultForRequest(
            @PathVariable Long requestId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Résultat IA récupéré avec succès.",
                        clientCollectionService.getAiResultForRequest(requestId)
                )
        );
    }

    @GetMapping("/requests/{requestId}/collection")
    public ResponseEntity<ApiResponse<CollectionResponseDTO>> getCollectionForRequest(
            @PathVariable Long requestId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Collecte liée à la demande récupérée avec succès.",
                        clientCollectionService.getCollectionForRequest(requestId)
                )
        );
    }

    @GetMapping("/requests/{requestId}/collector-location")
    public ResponseEntity<ApiResponse<CollectorLocationHistoryResponseDTO>> getCollectorLocationForRequest(
            @PathVariable Long requestId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Position actuelle du collecteur récupérée avec succès.",
                        clientCollectionService.getCollectorLocationForRequest(requestId)
                )
        );
    }


    // recyclix/backend/controller/client/ClientCollectionController.java

// Ajouter ces endpoints

    /**
     * Récupérer le code de validation d'une demande (pour l'afficher au client)
     */
    @GetMapping("/requests/{requestId}/validation-code")
    public ResponseEntity<ApiResponse<ClientValidationCodeResponse>> getValidationCode(
            @PathVariable Long requestId
    ) {
        CollectionRequest request = clientCollectionService.getOwnedRequest(requestId);

        ClientValidationCodeResponse response = ClientValidationCodeResponse.builder()
                .requestId(request.getId())
                .validationCode(request.getValidationCode())
                .codeStatus(request.getCodeStatus() != null ? request.getCodeStatus().name() : "PENDING")
                .codeGeneratedAt(request.getCodeGeneratedAt())
                .expiresAt(request.getCodeGeneratedAt() != null ? request.getCodeGeneratedAt().plusHours(48) : null)
                .canRefresh(request.getCodeStatus() != CollectionRequest.CodeStatus.USED
                        && request.getStatus() != CollectionRequest.Status.COLLECTED)
                .build();

        return ResponseEntity.ok(
                ApiResponse.ok("Code de validation récupéré avec succès.", response)
        );
    }

    /**
     * Générer un nouveau code de validation (si le client a perdu l'ancien)
     */
    @PostMapping("/requests/{requestId}/refresh-code")
    public ResponseEntity<ApiResponse<CollectionRequestResponseDTO>> refreshValidationCode(
            @PathVariable Long requestId
    ) {
        CollectionRequestResponseDTO response = clientCollectionService.refreshValidationCode(requestId);
        return ResponseEntity.ok(
                ApiResponse.ok("Nouveau code de validation généré avec succès.", response)
        );
    }

    //.-----------------------------------------------


    /**
     * Récupérer le code ET le QR code ensemble (version complète)
     */
    @GetMapping("/requests/{requestId}/validation-info")
    public ResponseEntity<ApiResponse<ClientValidationInfoResponse>> getValidationInfo(
            @PathVariable Long requestId
    ) {
        CollectionRequest request = clientCollectionService.getOwnedRequest(requestId);

        boolean isExpired = request.getCodeGeneratedAt() != null &&
                request.getCodeGeneratedAt().plusHours(48).isBefore(LocalDateTime.now());
        boolean isUsed = request.getCodeStatus() == CollectionRequest.CodeStatus.USED;

        ClientValidationInfoResponse response = ClientValidationInfoResponse.builder()
                .requestId(request.getId())
                .validationCode(request.getValidationCode())
                .codeStatus(request.getCodeStatus() != null ? request.getCodeStatus().name() : "PENDING")
                .codeGeneratedAt(request.getCodeGeneratedAt())
                .expiresAt(request.getCodeGeneratedAt() != null ? request.getCodeGeneratedAt().plusHours(48) : null)
                .canRefresh(request.getCodeStatus() != CollectionRequest.CodeStatus.USED
                        && request.getStatus() != CollectionRequest.Status.COLLECTED)
                .qrCodeBase64(qrCodeService.generateQRCodeAsBase64(request.getValidationCode()))
                .validationUrl(qrCodeService.generateValidationUrl(request.getValidationCode()))
                .isExpired(isExpired)
                .isUsed(isUsed)
                .build();

        return ResponseEntity.ok(
                ApiResponse.ok("Informations de validation récupérées avec succès.", response)
        );
    }




    @GetMapping("/requests/{requestId}/qrcode/base64")
    public ResponseEntity<ApiResponse<String>> getQRCodeBase64(
            @PathVariable Long requestId
    ) {
        CollectionRequest request = clientCollectionService.getOwnedRequest(requestId);

        if (request.getValidationCode() == null) {
            throw new BadRequestException("Aucun code de validation généré pour cette demande.");
        }

        // Utilise la version avec logo
        String qrCodeBase64 = qrCodeService.generateQRCodeAsBase64(request.getValidationCode());

        return ResponseEntity.ok(
                ApiResponse.ok("QR code généré avec succès.", qrCodeBase64)
        );
    }

    @GetMapping("/requests/{requestId}/qrcode/download")
    public ResponseEntity<Resource> downloadQRCode(
            @PathVariable Long requestId
    ) {
        CollectionRequest request = clientCollectionService.getOwnedRequest(requestId);

        if (request.getValidationCode() == null) {
            throw new BadRequestException("Aucun code de validation généré pour cette demande.");
        }

        String filename = "code_validation_recyclix_demande_" + requestId;

        return qrCodeService.downloadQRCode(request.getValidationCode(), filename);
    }


    // recyclix/backend/controller/client/ClientCollectionController.java

    @GetMapping("/requests/{requestId}/qrcode/compact")
    public ResponseEntity<ApiResponse<String>> getCompactQRCodeBase64(
            @PathVariable Long requestId
    ) {
        CollectionRequest request = clientCollectionService.getOwnedRequest(requestId);

        if (request.getValidationCode() == null) {
            throw new BadRequestException("Aucun code de validation généré pour cette demande.");
        }

        byte[] qrCodeBytes = qrCodeService.generateCompactQRCode(request.getValidationCode());
        String qrCodeBase64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(qrCodeBytes);

        return ResponseEntity.ok(
                ApiResponse.ok("QR code compact généré avec succès.", qrCodeBase64)
        );
    }


    // Dans ClientCollectionController.java

    @PutMapping("/requests/{requestId}/cancel-after-accept")
    public ResponseEntity<ApiResponse<CollectionRequestResponseDTO>> cancelAfterAccept(@PathVariable Long requestId) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Demande annulée avec succès (après acceptation).",
                        clientCollectionService.cancelAfterAccept(requestId)
                )
        );
    }
}