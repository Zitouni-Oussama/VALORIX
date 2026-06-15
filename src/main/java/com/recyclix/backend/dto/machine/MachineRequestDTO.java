package com.recyclix.backend.dto.machine;

import com.recyclix.backend.model.Machine;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MachineRequestDTO {

    @NotNull(message = "Le nom de la machine ne peut pas être nul.")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères.")
    private String name;

    @NotNull(message = "Le type de la machine ne peut pas être nul.")
    @Size(max = 100, message = "Le type ne peut pas dépasser 100 caractères.")
    private String type;

    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères.")
    private String description;

    @Size(max = 255, message = "L'URL de la photo ne peut pas dépasser 255 caractères.")
    private String photoUrl;

    private Machine.MachineStatus status;

    private LocalDate lastMaintenanceDate;

    @NotNull(message = "Le numéro de série ne peut pas être nul.")
    @Size(max = 100, message = "Le numéro de série ne peut pas dépasser 100 caractères.")
    private String serialNumber;
}