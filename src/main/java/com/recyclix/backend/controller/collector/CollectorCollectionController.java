package com.recyclix.backend.controller.collector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recyclix.backend.dto.collection.CollectionResponseDTO;
import com.recyclix.backend.dto.collection.CollectionSummaryDTO;
import com.recyclix.backend.dto.collection_request.CollectionRequestResponseDTO;
import com.recyclix.backend.dto.collection_request.CollectionRequestSummaryDTO;
import com.recyclix.backend.dto.collector_location_history.CollectorLocationHistoryResponseDTO;
import com.recyclix.backend.dto.collector_location_history.CollectorLocationHistoryUpdateDTO;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.model.CollectionRequest;
import com.recyclix.backend.repository.CollectionRequestRepository;
import com.recyclix.backend.service.collector.CollectorCollectionService;
import com.recyclix.backend.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

// Ajouter ces imports
import com.recyclix.backend.dto.collection.ValidationCodeResponseDTO;
import com.recyclix.backend.dto.collection_request.ValidationCodeRequestDTO;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/collector/collections")
@RequiredArgsConstructor
@PreAuthorize("hasRole('COLLECTOR')")
public class CollectorCollectionController {

    private final CollectorCollectionService collectorCollectionService;
    private final ObjectMapper objectMapper;
    private final CollectionRequestRepository collectionRequestRepository;

    @GetMapping("/available-requests")
    public ResponseEntity<ApiResponse<List<CollectionRequestSummaryDTO>>> getAvailableRequests() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Demandes disponibles récupérées avec succès.",
                        collectorCollectionService.getAvailableRequests()
                )
        );
    }

    @PostMapping("/requests/{requestId}/accept")
    public ResponseEntity<ApiResponse<CollectionRequestResponseDTO>> acceptRequest(
            @PathVariable Long requestId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Demande acceptée avec succès.",
                        collectorCollectionService.acceptRequest(requestId)
                )
        );
    }

    @GetMapping("/my-active")
    public ResponseEntity<ApiResponse<List<CollectionRequestSummaryDTO>>> getMyActiveRequests() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Mes missions actives récupérées avec succès.",
                        collectorCollectionService.getMyActiveRequests()
                )
        );
    }

    @GetMapping("/my-history")
    public ResponseEntity<ApiResponse<List<CollectionSummaryDTO>>> getMyCollectionsHistory() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Historique des collectes récupéré avec succès.",
                        collectorCollectionService.getMyCollectionsHistory()
                )
        );
    }

    @GetMapping("/requests/{requestId}")
    public ResponseEntity<ApiResponse<CollectionRequestResponseDTO>> getAcceptedRequestDetails(
            @PathVariable Long requestId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Détail de la mission récupéré avec succès.",
                        collectorCollectionService.getAcceptedRequestDetails(requestId)
                )
        );
    }

    @GetMapping("/{collectionId}")
    public ResponseEntity<ApiResponse<CollectionResponseDTO>> getMyCollectionDetails(
            @PathVariable Long collectionId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Détail de la collecte récupéré avec succès.",
                        collectorCollectionService.getMyCollectionDetails(collectionId)
                )
        );
    }

    @PostMapping(value = "/requests/{requestId}/complete", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CollectionResponseDTO>> completeCollection(
            @PathVariable Long requestId,
            @RequestPart("collection") String collectionJson,
            @RequestPart(value = "collectionProofImage", required = false) MultipartFile collectionProofImage
    ) throws Exception {
        CollectorCollectionService.CompleteCollectionRequest request =
                objectMapper.readValue(collectionJson, CollectorCollectionService.CompleteCollectionRequest.class);

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Collecte terminée avec succès.",
                        collectorCollectionService.completeCollection(requestId, request, collectionProofImage)
                )
        );
    }

    @PutMapping("/my-location")
    public ResponseEntity<ApiResponse<CollectorLocationHistoryResponseDTO>> updateMyCurrentLocation(
            @RequestBody CollectorLocationHistoryUpdateDTO dto
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Position actuelle mise à jour avec succès.",
                        collectorCollectionService.updateMyCurrentLocation(dto)
                )
        );
    }

    @GetMapping("/my-location")
    public ResponseEntity<ApiResponse<CollectorLocationHistoryResponseDTO>> getMyCurrentLocation() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Position actuelle récupérée avec succès.",
                        collectorCollectionService.getMyCurrentLocation()
                )
        );
    }


    // recyclix/backend/controller/collector/CollectorCollectionController.java

    /**
     * Valider une collecte avec un code de validation (saisie manuelle ou scan QR)
     */
    @PostMapping("/requests/{requestId}/validate-with-code")
    public ResponseEntity<ApiResponse<ValidationCodeResponseDTO>> validateWithCode(
            @PathVariable Long requestId,
            @Valid @RequestBody ValidationCodeRequestDTO codeRequest
    ) {
        ValidationCodeResponseDTO response = collectorCollectionService.validateCollectionWithCode(requestId, codeRequest);
        return ResponseEntity.ok(
                ApiResponse.ok(response.getMessage(), response)
        );
    }

    /**
     * Scanner un QR code directement (endpoint simplifié pour le scan)
     * Alternative: GET avec le code dans l'URL pour les scans QR
     */
    @GetMapping("/validate/scan")
    public ResponseEntity<ApiResponse<ValidationCodeResponseDTO>> scanValidationCode(
            @RequestParam String code
    ) {
        // Chercher la demande associée à ce code
        CollectionRequest request = collectionRequestRepository.findByValidationCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Code de validation invalide."));

        ValidationCodeResponseDTO response = collectorCollectionService.validateCollectionWithCode(
                request.getId(),
                ValidationCodeRequestDTO.builder().validationCode(code).build()
        );

        return ResponseEntity.ok(
                ApiResponse.ok(response.getMessage(), response)
        );
    }

    //.------------------------------
    // recyclix/backend/controller/collector/CollectorCollectionController.java

// Ajouter cet endpoint pour le scan depuis l'app mobile du collecteur

    /**
     * Scanner un QR code et valider automatiquement (pour caméra mobile)
     * Format attendu dans le QR code: l'URL complète ou juste le code
     */
    @PostMapping("/validate/scan-qr")
    public ResponseEntity<ApiResponse<ValidationCodeResponseDTO>> scanQRCodeAndValidate(
            @RequestBody ScanQRCodeRequest request
    ) {
        // Extraire le code de validation de l'URL ou du texte scanné
        String scannedData = request.getScannedData();
        String validationCode = extractValidationCodeFromScannedData(scannedData);

        // Chercher la demande associée à ce code
        CollectionRequest collectionRequest = collectionRequestRepository
                .findByValidationCode(validationCode)
                .orElseThrow(() -> new ResourceNotFoundException("Code de validation invalide."));

        ValidationCodeResponseDTO response = collectorCollectionService.validateCollectionWithCode(
                collectionRequest.getId(),
                ValidationCodeRequestDTO.builder().validationCode(validationCode).build()
        );

        return ResponseEntity.ok(
                ApiResponse.ok(response.getMessage(), response)
        );
    }

    // Classe interne pour la requête
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ScanQRCodeRequest {
        private String scannedData;
    }

    // Méthode utilitaire pour extraire le code
    private String extractValidationCodeFromScannedData(String scannedData) {
        if (scannedData == null || scannedData.isBlank()) {
            throw new BadRequestException("Données du QR code invalides.");
        }

        // Format d'URL: https://recyclix.com/validate?code=RX-260503-A3F9
        if (scannedData.contains("code=")) {
            String[] parts = scannedData.split("code=");
            if (parts.length > 1) {
                String codePart = parts[1];
                // Supprimer les paramètres supplémentaires
                int ampersandIndex = codePart.indexOf('&');
                if (ampersandIndex > 0) {
                    codePart = codePart.substring(0, ampersandIndex);
                }
                return codePart;
            }
        }

        // Si c'est directement le code (format RX-YYMMDD-XXXX)
        if (scannedData.matches("RX-\\d{6}-[A-Z0-9]{4}")) {
            return scannedData;
        }

        throw new BadRequestException("Format de QR code invalide.");
    }

    // Dans CollectorCollectionController.java

    @PutMapping("/requests/{requestId}/cancel-acceptance")
    public ResponseEntity<ApiResponse<CollectionRequestResponseDTO>> cancelAcceptance(@PathVariable Long requestId) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Acceptation annulée. La demande redevient disponible.",
                        collectorCollectionService.cancelAcceptance(requestId)
                )
        );
    }
}