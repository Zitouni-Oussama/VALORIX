package com.recyclix.backend.dto.machine_incident;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MachineIncidentUpdateDTO {

    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères.")
    private String description;

    private String severity;
    private String status;

    private LocalDateTime resolvedAt;
    private String incidentImageUrl;
}