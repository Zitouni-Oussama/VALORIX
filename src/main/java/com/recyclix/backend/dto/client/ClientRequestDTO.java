package com.recyclix.backend.dto.client;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientRequestDTO {

    @NotNull(message = "Le accountId ne peut pas être nul.")
    private Long accountId;

    @NotNull(message = "Le prénom ne peut pas être nul.")
    @Size(max = 100, message = "Le prénom ne peut pas dépasser 100 caractères.")
    private String firstName;

    @NotNull(message = "Le nom ne peut pas être nul.")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères.")
    private String lastName;

    @Size(max = 255, message = "L'adresse ne peut pas dépasser 255 caractères.")
    private String address;

    private BigDecimal latitude;
    private BigDecimal longitude;

    // optionnel : si null -> @PrePersist met 0
    private Integer totalPoints;
}