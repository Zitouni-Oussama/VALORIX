package com.recyclix.backend.dto.machine_incident;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MachineIncidentSummaryDTO {

    private Long id;

    private Long machineId;
    private String severity;
    private String status;

    private LocalDateTime reportedAt;
    private String incidentImageUrl;

    private String machineName;
    private String description;
}