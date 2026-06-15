package com.recyclix.backend.dto.client;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientUpdateDTO {

    @Size(max = 100, message = "Le prénom ne peut pas dépasser 100 caractères.")
    private String firstName;

    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères.")
    private String lastName;

    @Size(max = 255, message = "L'adresse ne peut pas dépasser 255 caractères.")
    private String address;

    private BigDecimal latitude;
    private BigDecimal longitude;

    private Integer totalPoints;
}