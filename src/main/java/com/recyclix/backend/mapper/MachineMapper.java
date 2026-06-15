package com.recyclix.backend.mapper;

import com.recyclix.backend.dto.machine.MachineRequestDTO;
import com.recyclix.backend.dto.machine.MachineResponseDTO;
import com.recyclix.backend.dto.machine.MachineSummaryDTO;
import com.recyclix.backend.dto.machine.MachineUpdateDTO;
import com.recyclix.backend.model.Machine;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface MachineMapper {

    // =========================
    // Entity -> Response DTO
    // =========================
    @Mappings({
            @Mapping(target = "type", ignore = true),
            @Mapping(target = "lastMaintenanceDate", ignore = true),
            @Mapping(target = "incidentsCount", expression = "java(entity.getIncidents() != null ? entity.getIncidents().size() : 0)"),
            // MapStruct gère automatiquement le mapping de l'enum MachineStatus -> MachineStatus
            @Mapping(target = "status", source = "status"),
            @Mapping(target = "description", source = "description"),
            @Mapping(target = "photoUrl", source = "photoUrl")
    })
    MachineResponseDTO toDto(Machine entity);

    // =========================
    // Entity -> Summary DTO
    // =========================
    @Mappings({
            @Mapping(target = "status", source = "status"),
            @Mapping(target = "description", source = "description"),
            @Mapping(target = "photoUrl", source = "photoUrl"),
            @Mapping(target = "serialNumber", source = "serialNumber")
    })
    MachineSummaryDTO toSummaryDto(Machine entity);

    // =========================
    // Request DTO -> Entity
    // =========================
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "serialNumber", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "incidents", ignore = true),
            @Mapping(target = "status", source = "status"), // mapping direct de l'enum
            @Mapping(target = "description", source = "description"),
            @Mapping(target = "photoUrl", source = "photoUrl")
    })
    Machine toEntity(MachineRequestDTO dto);

    // =========================
    // Update DTO -> existing Entity
    // =========================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "serialNumber", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "incidents", ignore = true),
            @Mapping(target = "status", source = "status"),
            @Mapping(target = "description", source = "description"),
            @Mapping(target = "photoUrl", source = "photoUrl")
    })
    void updateEntityFromDto(MachineUpdateDTO dto, @MappingTarget Machine entity);
}