// recyclix/backend/dto/collection_request/ValidationCodeRequestDTO.java
package com.recyclix.backend.dto.collection_request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationCodeRequestDTO {

    @NotBlank(message = "Le code de validation est obligatoire")
    private String validationCode;

    private BigDecimal realQuantity;
}