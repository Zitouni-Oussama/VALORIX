package com.recyclix.backend.mapper;

import com.recyclix.backend.dto.workshop.MaterialStockDTO;
import com.recyclix.backend.model.MaterialStock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MaterialStockMapper {
    @Mapping(target = "materialId", source = "material.id")
    @Mapping(target = "materialName", source = "material.name")
    MaterialStockDTO toDto(MaterialStock entity);
}