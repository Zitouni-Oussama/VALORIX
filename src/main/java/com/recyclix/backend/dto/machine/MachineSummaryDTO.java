package com.recyclix.backend.dto.machine;

import com.recyclix.backend.model.Machine;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MachineSummaryDTO {

    private Long id;
    private String name;
    private Machine.MachineStatus status;
    private LocalDateTime createdAt;
    private String description;
    private String photoUrl;
    private String serialNumber;
}