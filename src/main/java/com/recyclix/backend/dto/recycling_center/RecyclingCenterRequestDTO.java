package com.recyclix.backend.dto.recycling_center;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecyclingCenterRequestDTO {

    @NotNull(message = "Le nom ne peut pas être nul.")
    @Size(max = 150, message = "Le nom ne peut pas dépasser 150 caractères.")
    private String name;

    @NotNull(message = "L'adresse ne peut pas être nulle.")
    @Size(max = 255, message = "L'adresse ne peut pas dépasser 255 caractères.")
    private String address;

    private BigDecimal latitude;
    private BigDecimal longitude;

    @Size(max = 150, message = "L'email ne peut pas dépasser 150 caractères.")
    private String contactEmail;

    @Size(max = 20, message = "Le téléphone ne peut pas dépasser 20 caractères.")
    private String contactPhone;

    @NotNull(message = "La capacité est obligatoire.")
    private BigDecimal capacity;
}