package com.recyclix.backend.dto.truck;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TruckRequestDTO {

    @NotNull(message = "Le numéro de plaque ne peut pas être nul.")
    @Size(max = 20, message = "La plaque ne peut pas dépasser 20 caractères.")
    private String plateNumber;

    @Size(max = 100, message = "Le modèle ne peut pas dépasser 100 caractères.")
    private String model;

    @NotNull(message = "La capacité ne peut pas être nulle.")
    private BigDecimal capacityKg;

    @NotNull(message = "Le statut ne peut pas être nul.")
    private String status;

    private String brand;

    private Long collectorId;
    
    private String truckPhotoUrl;
    private String greyCardImageUrl;
    private String drivingLicenseImageUrl;

}