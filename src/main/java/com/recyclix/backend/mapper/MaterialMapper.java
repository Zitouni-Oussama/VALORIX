package com.recyclix.backend.mapper;

import com.recyclix.backend.dto.material.MaterialRequestDTO;
import com.recyclix.backend.dto.material.MaterialResponseDTO;
import com.recyclix.backend.dto.material.MaterialSummaryDTO;
import com.recyclix.backend.dto.material.MaterialUpdateDTO;
import com.recyclix.backend.model.Material;
import org.mapstruct.*;

import java.math.BigDecimal;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface MaterialMapper {

    // =========================
    // Entity -> Response DTO
    // =========================
    @Mappings({
            @Mapping(target = "basePricePerKg", expression = "java(extractBasePrice(entity))"),
            @Mapping(target = "collectionRequestsCount", expression = "java(entity.getCollectionRequests() != null ? entity.getCollectionRequests().size() : 0)"),
            @Mapping(target = "aiClassificationsCount", ignore = true)
    })
    MaterialResponseDTO toDto(Material entity);

    // =========================
    // Entity -> Summary DTO
    // =========================
    @Mapping(target = "basePricePerKg", expression = "java(extractBasePrice(entity))")
    @Mapping(target = "collectorPricePerKg", expression = "java(extractCollectorPrice(entity))")
    MaterialSummaryDTO toSummaryDto(Material entity);

    // =========================
    // Request DTO -> Entity
    // =========================
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "materialPrice", ignore = true),
            @Mapping(target = "collectionRequests", ignore = true)
    })
    Material toEntity(MaterialRequestDTO dto);

    // =========================
    // Update DTO -> existing Entity
    // =========================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "materialPrice", ignore = true),
            @Mapping(target = "collectionRequests", ignore = true)
    })
    void updateEntityFromDto(MaterialUpdateDTO dto, @MappingTarget Material entity);

    default BigDecimal extractBasePrice(Material entity) {
        return entity != null
                && entity.getMaterialPrice() != null
                ? entity.getMaterialPrice().getCitizenPricePerKg()
                : null;
    }

    default BigDecimal extractCollectorPrice(Material entity) {
        return entity != null && entity.getMaterialPrice() != null
                ? entity.getMaterialPrice().getCollectorPricePerKg()
                : null;
    }
}