package com.recyclix.backend.dto.worker_absence;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerAbsenceRequestDTO {

    @NotNull(message = "L'employé est obligatoire.")
    private Long employeeId;

    @NotNull(message = "Le type d'absence ne peut pas être nul.")
    private String type;

    @NotNull(message = "La date de début ne peut pas être nulle.")
    private LocalDate startDate;

    private LocalDate endDate;

    @Size(max = 500)
    private String reason;
}