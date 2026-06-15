package com.recyclix.backend.dto.point_movement;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointMovementResponseDTO {

    private Long id;

    private Long accountId;
    private Long collectionId;

    private String type;
    private Integer points;
    private String description;

    private LocalDateTime createdAt;
}