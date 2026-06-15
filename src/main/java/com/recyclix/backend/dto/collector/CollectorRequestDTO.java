package com.recyclix.backend.dto.collector;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectorRequestDTO {

    @NotNull(message = "Le accountId ne peut pas être nul.")
    private Long accountId;

    @NotNull(message = "Le prénom ne peut pas être nul.")
    @Size(max = 100, message = "Le prénom ne peut pas dépasser 100 caractères.")
    private String firstName;

    @NotNull(message = "Le nom ne peut pas être nul.")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères.")
    private String lastName;

    @NotNull(message = "Le numéro d'identification national ne peut pas être nul.")
    @Size(max = 50, message = "Le numéro d'identification national ne peut pas dépasser 50 caractères.")
    private String nationalIdNumber;

    // optionnel : si null -> @PrePersist met false
    private Boolean isVerified;

    // optionnel : si null -> @PrePersist met 0
    private BigDecimal averageRating;

    private BigDecimal currentLatitude;
    private BigDecimal currentLongitude;
}