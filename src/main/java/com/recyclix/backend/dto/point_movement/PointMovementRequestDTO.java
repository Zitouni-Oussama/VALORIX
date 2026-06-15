package com.recyclix.backend.dto.point_movement;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointMovementRequestDTO {

    @NotNull(message = "Le accountId ne peut pas être nul.")
    private Long accountId;

    @NotNull(message = "Le type ne peut pas être nul.")
    private String type;

    @NotNull(message = "Le nombre de points ne peut pas être nul.")
    private Integer points;

    @Size(max = 255, message = "La description ne peut pas dépasser 255 caractères.")
    private String description;

    private Long collectionId;
}