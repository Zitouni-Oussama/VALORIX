package com.recyclix.backend.dto.point_movement;

import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointMovementUpdateDTO {

    @Size(max = 255, message = "La description ne peut pas dépasser 255 caractères.")
    private String description;
}