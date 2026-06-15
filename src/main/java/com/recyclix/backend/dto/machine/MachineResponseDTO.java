package com.recyclix.backend.dto.machine;

import com.recyclix.backend.model.Machine;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MachineResponseDTO {

    private Long id;

    private String name;
    private String type;
    private Machine.MachineStatus status;

    private LocalDate lastMaintenanceDate;
    private LocalDateTime createdAt;

    private Integer incidentsCount;

    private String description;
    private String photoUrl;


}