// recyclix/backend/dto/collection_request/ClientValidationCodeResponse.java
package com.recyclix.backend.dto.collection_request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientValidationCodeResponse {

    private Long requestId;
    private String validationCode;
    private String codeStatus;
    private LocalDateTime codeGeneratedAt;
    private LocalDateTime expiresAt;
    private boolean canRefresh;
    private String qrCodeDataUrl; // Optionnel: pour générer le QR code côté serveur

    // Ajouter ces champs
    private String qrCodeBase64;
    private String validationUrl;

}