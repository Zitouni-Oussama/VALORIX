package com.recyclix.backend.dto.worker_absence;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerAbsenceResponseDTO {

    private Long id;

    private Long employeeId;
    private String employeeFullName;

    private String type;

    private LocalDate startDate;
    private LocalDate endDate;

    private String reason;

    private LocalDateTime createdAt;
}