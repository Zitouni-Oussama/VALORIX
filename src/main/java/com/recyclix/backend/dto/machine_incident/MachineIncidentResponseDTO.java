package com.recyclix.backend.dto.machine_incident;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MachineIncidentResponseDTO {

    private Long id;

    private Long machineId;
    private Long reportedById;
    private String incidentImageUrl;

    private String description;
    private String severity;
    private String status;

    private LocalDateTime reportedAt;
    private LocalDateTime resolvedAt;
}