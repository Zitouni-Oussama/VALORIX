package com.recyclix.backend.mapper;

import com.recyclix.backend.dto.worker_absence.WorkerAbsenceRequestDTO;
import com.recyclix.backend.dto.worker_absence.WorkerAbsenceResponseDTO;
import com.recyclix.backend.dto.worker_absence.WorkerAbsenceSummaryDTO;
import com.recyclix.backend.dto.worker_absence.WorkerAbsenceUpdateDTO;
import com.recyclix.backend.model.WorkerAbsence;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WorkerAbsenceMapper {

    @Mappings({
            @Mapping(target = "employeeId", expression = "java(entity.getEmployee() != null ? entity.getEmployee().getId() : null)"),
            @Mapping(target = "employeeFullName", expression = "java(entity.getEmployee() != null ? entity.getEmployee().getFullName() : null)")
    })
    WorkerAbsenceResponseDTO toResponseDTO(WorkerAbsence entity);

    @Mappings({
            @Mapping(target = "employeeId", expression = "java(entity.getEmployee() != null ? entity.getEmployee().getId() : null)"),
            @Mapping(target = "employeeFullName", expression = "java(entity.getEmployee() != null ? entity.getEmployee().getFullName() : null)")
    })
    WorkerAbsenceSummaryDTO toSummaryDTO(WorkerAbsence entity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "employee", ignore = true),
            @Mapping(target = "recordedBy", ignore = true),
            @Mapping(target = "createdAt", ignore = true)
    })
    WorkerAbsence toEntity(WorkerAbsenceRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "employee", ignore = true),
            @Mapping(target = "recordedBy", ignore = true),
            @Mapping(target = "createdAt", ignore = true)
    })
    void updateEntityFromDTO(WorkerAbsenceUpdateDTO dto, @MappingTarget WorkerAbsence entity);
}