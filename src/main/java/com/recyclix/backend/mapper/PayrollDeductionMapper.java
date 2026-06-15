package com.recyclix.backend.mapper;

import com.recyclix.backend.dto.payroll.DeductionRequestDTO;
import com.recyclix.backend.dto.payroll.DeductionResponseDTO;
import com.recyclix.backend.dto.payroll.DeductionSummaryDTO;
import com.recyclix.backend.dto.payroll.DeductionUpdateDTO;
import com.recyclix.backend.model.PayrollDeduction;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PayrollDeductionMapper {

    @Mappings({
            @Mapping(target = "employeeId", expression = "java(entity.getEmployee() != null ? entity.getEmployee().getId() : null)"),
            @Mapping(target = "employeeFullName", expression = "java(entity.getEmployee() != null ? entity.getEmployee().getFullName() : null)"),
            @Mapping(target = "recordedById", expression = "java(entity.getRecordedBy() != null ? entity.getRecordedBy().getId() : null)"),
            @Mapping(target = "recordedByFullName", expression = "java(entity.getRecordedBy() != null ? entity.getRecordedBy().getFirstName() + \" \" + entity.getRecordedBy().getLastName() : null)")
    })
    DeductionResponseDTO toResponseDTO(PayrollDeduction entity);

    @Mappings({
            @Mapping(target = "employeeId", expression = "java(entity.getEmployee() != null ? entity.getEmployee().getId() : null)"),
            @Mapping(target = "employeeFullName", expression = "java(entity.getEmployee() != null ? entity.getEmployee().getFullName() : null)")
    })
    DeductionSummaryDTO toSummaryDTO(PayrollDeduction entity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "employee", ignore = true),
            @Mapping(target = "recordedBy", ignore = true),
            @Mapping(target = "createdAt", ignore = true)
    })
    PayrollDeduction toEntity(DeductionRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "employee", ignore = true),
            @Mapping(target = "recordedBy", ignore = true),
            @Mapping(target = "createdAt", ignore = true)
    })
    void updateEntityFromDTO(DeductionUpdateDTO dto, @MappingTarget PayrollDeduction entity);
}