package com.recyclix.backend.dto.machine_incident;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MachineIncidentRequestDTO {

    @NotNull(message = "Le machineId ne peut pas être nul.")
    private Long machineId;

    @NotNull(message = "Le reportedById ne peut pas être nul.")
    private Long reportedById;

    @NotNull(message = "La description ne peut pas être nulle.")
    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères.")
    private String description;

    @NotNull(message = "La sévérité ne peut pas être nulle.")
    private String severity;

    @NotNull(message = "Le statut ne peut pas être nul.")
    private String status;

    private String incidentImageUrl;
}