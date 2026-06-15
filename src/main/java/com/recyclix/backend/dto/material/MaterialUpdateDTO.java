package com.recyclix.backend.dto.material;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialUpdateDTO {

    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères.")
    private String name;

    @Size(max = 255, message = "La description ne peut pas dépasser 255 caractères.")
    private String description;

    private BigDecimal basePricePerKg;

    private Boolean isActive;
}