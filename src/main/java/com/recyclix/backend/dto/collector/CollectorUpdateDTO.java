package com.recyclix.backend.dto.collector;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectorUpdateDTO {

    @Size(max = 100, message = "Le prénom ne peut pas dépasser 100 caractères.")
    private String firstName;

    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères.")
    private String lastName;

    @Size(max = 50, message = "Le numéro d'identification national ne peut pas dépasser 50 caractères.")
    private String nationalIdNumber;

    private Boolean isVerified;

    private BigDecimal averageRating;

    private BigDecimal currentLatitude;
    private BigDecimal currentLongitude;
}