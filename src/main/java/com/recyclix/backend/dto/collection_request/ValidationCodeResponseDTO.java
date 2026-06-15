// recyclix/backend/dto/collection/ValidationCodeResponseDTO.java
package com.recyclix.backend.dto.collection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationCodeResponseDTO {

    private boolean success;
    private String message;
    private Long collectionId;
    private String requestStatus;
    private String codeStatus;
}