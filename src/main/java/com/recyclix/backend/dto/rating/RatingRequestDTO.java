package com.recyclix.backend.dto.rating;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingRequestDTO {

    @NotNull(message = "La note est obligatoire.")
    @DecimalMin(value = "0.0", message = "La note doit être comprise entre 0 et 5.")
    @DecimalMax(value = "5.0", message = "La note doit être comprise entre 0 et 5.")
    private BigDecimal rating;

    @Size(max = 500, message = "Le commentaire ne peut pas dépasser 500 caractères.")
    private String comment;
}