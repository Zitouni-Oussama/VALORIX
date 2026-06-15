package com.recyclix.backend.dto.worker_absence;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerAbsenceUpdateDTO {

    private Long employeeId;

    private String type;

    private LocalDate startDate;

    private LocalDate endDate;

    @Size(max = 500)
    private String reason;
}