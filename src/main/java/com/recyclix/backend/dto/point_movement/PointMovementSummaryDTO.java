package com.recyclix.backend.dto.point_movement;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointMovementSummaryDTO {

    private Long id;

    private Integer points;
    private String type;

    private LocalDateTime createdAt;
}