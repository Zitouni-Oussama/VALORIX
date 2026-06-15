package com.recyclix.backend.mapper;

import com.recyclix.backend.dto.workshop.TreatmentBatchDTO;
import com.recyclix.backend.model.TreatmentBatch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TreatmentBatchMapper {
    @Mapping(target = "materialId", source = "material.id")
    @Mapping(target = "materialName", source = "material.name")
    @Mapping(target = "startedBy", expression = "java(entity.getStartedBy().getFirstName() + \" \" + entity.getStartedBy().getLastName())")
    @Mapping(target = "status", expression = "java(entity.getStatus().name())")
    TreatmentBatchDTO toDto(TreatmentBatch entity);
}