package com.recyclix.backend.mapper;

import com.recyclix.backend.dto.employee.EmployeeRequestDTO;
import com.recyclix.backend.dto.employee.EmployeeResponseDTO;
import com.recyclix.backend.dto.employee.EmployeeSummaryDTO;
import com.recyclix.backend.dto.employee.EmployeeUpdateDTO;
import com.recyclix.backend.model.Employee;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmployeeMapper {

    @Mapping(target = "fullName", expression = "java(entity.getFullName())")
    @Mapping(target = "recyclingCenterId", expression = "java(entity.getRecyclingCenter() != null ? entity.getRecyclingCenter().getId() : null)")
    @Mapping(target = "recyclingCenterName", expression = "java(entity.getRecyclingCenter() != null ? entity.getRecyclingCenter().getName() : null)")
    EmployeeResponseDTO toResponseDTO(Employee entity);

    @Mapping(target = "fullName", expression = "java(entity.getFullName())")
    @Mapping(target = "recyclingCenterName", expression = "java(entity.getRecyclingCenter() != null ? entity.getRecyclingCenter().getName() : null)")
    EmployeeSummaryDTO toSummaryDTO(Employee entity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "active", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true)
    })
    Employee toEntity(EmployeeRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true)
    })
    void updateEntityFromDTO(EmployeeUpdateDTO dto, @MappingTarget Employee entity);
}