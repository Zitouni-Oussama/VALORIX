package com.recyclix.backend.dto.machine;

import com.recyclix.backend.model.Machine;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MachineUpdateDTO {

    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères.")
    private String name;

    @Size(max = 100, message = "Le type ne peut pas dépasser 100 caractères.")
    private String type;

    private Machine.MachineStatus status;

    private LocalDate lastMaintenanceDate;

    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères.")
    private String description;

    @Size(max = 255, message = "L'URL de la photo ne peut pas dépasser 255 caractères.")
    private String photoUrl;

    @Size(max = 100, message = "Le numéro de série ne peut pas dépasser 100 caractères.")
    private String serialNumber;
}