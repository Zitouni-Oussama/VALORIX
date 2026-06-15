package com.recyclix.backend.dto.worker_absence;

import lombok.*;

import java.time.LocalDate;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerAbsenceSummaryDTO {

    private Long id;

    private Long employeeId;
    private String employeeFullName;

    private String type;

    private LocalDate startDate;
    private LocalDate endDate;
}