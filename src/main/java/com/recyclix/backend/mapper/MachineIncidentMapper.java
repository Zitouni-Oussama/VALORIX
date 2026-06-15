package com.recyclix.backend.mapper;

import com.recyclix.backend.dto.machine_incident.MachineIncidentRequestDTO;
import com.recyclix.backend.dto.machine_incident.MachineIncidentResponseDTO;
import com.recyclix.backend.dto.machine_incident.MachineIncidentSummaryDTO;
import com.recyclix.backend.dto.machine_incident.MachineIncidentUpdateDTO;
import com.recyclix.backend.model.MachineIncident;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface MachineIncidentMapper {

    // =========================
    // Entity -> Response DTO
    // =========================
    @Mappings({
            @Mapping(target = "machineId", expression = "java(entity.getMachine() != null ? entity.getMachine().getId() : null)"),
            @Mapping(target = "reportedById", expression = "java(entity.getReportedBy() != null ? entity.getReportedBy().getId() : null)"),
            @Mapping(target = "description", source = "issueType"),
            @Mapping(target = "severity", expression = "java(entity.getSeverity() != null ? entity.getSeverity().name() : null)"),
            @Mapping(target = "status", expression = "java(entity.getStatus() != null ? entity.getStatus().name() : null)")
    })
    MachineIncidentResponseDTO toDto(MachineIncident entity);

    // =========================
    // Entity -> Summary DTO
    // =========================
    @Mappings({
            @Mapping(target = "machineId", expression = "java(entity.getMachine() != null ? entity.getMachine().getId() : null)"),
            @Mapping(target = "severity", expression = "java(entity.getSeverity() != null ? entity.getSeverity().name() : null)"),
            @Mapping(target = "status", expression = "java(entity.getStatus() != null ? entity.getStatus().name() : null)"),
            @Mapping(target = "machineName", source = "machine.name"),
            @Mapping(target = "description", source = "issueType")
    })
    MachineIncidentSummaryDTO toSummaryDto(MachineIncident entity);

    // =========================
    // Request DTO -> Entity
    // =========================
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "issueType", source = "description"),
            @Mapping(target = "severity", expression = "java(dto.getSeverity() != null ? com.recyclix.backend.model.MachineIncident.IncidentSeverity.valueOf(dto.getSeverity().toUpperCase()) : null)"),
            @Mapping(target = "status", expression = "java(dto.getStatus() != null ? com.recyclix.backend.model.MachineIncident.IncidentStatus.valueOf(dto.getStatus().toUpperCase()) : null)"),
            @Mapping(target = "incidentImageUrl", ignore = true),
            @Mapping(target = "reportedAt", ignore = true),
            @Mapping(target = "resolvedAt", ignore = true),
            @Mapping(target = "machine", ignore = true),
            @Mapping(target = "reportedBy", ignore = true)
    })
    MachineIncident toEntity(MachineIncidentRequestDTO dto);

    // =========================
    // Update DTO -> existing Entity
    // =========================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "issueType", source = "description"),
            @Mapping(target = "severity", expression = "java(dto.getSeverity() != null ? com.recyclix.backend.model.MachineIncident.IncidentSeverity.valueOf(dto.getSeverity().toUpperCase()) : entity.getSeverity())"),
            @Mapping(target = "status", expression = "java(dto.getStatus() != null ? com.recyclix.backend.model.MachineIncident.IncidentStatus.valueOf(dto.getStatus().toUpperCase()) : entity.getStatus())"),
            @Mapping(target = "incidentImageUrl", ignore = true),
            @Mapping(target = "reportedAt", ignore = true),
            @Mapping(target = "machine", ignore = true),
            @Mapping(target = "reportedBy", ignore = true)
    })
    void updateEntityFromDto(MachineIncidentUpdateDTO dto, @MappingTarget MachineIncident entity);
}