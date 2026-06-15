package com.recyclix.backend.mapper;

import com.recyclix.backend.dto.material_price.MaterialPriceRequestDTO;
import com.recyclix.backend.dto.material_price.MaterialPriceResponseDTO;
import com.recyclix.backend.dto.material_price.MaterialPriceSummaryDTO;
import com.recyclix.backend.dto.material_price.MaterialPriceUpdateDTO;
import com.recyclix.backend.model.MaterialPrice;
import org.mapstruct.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface MaterialPriceMapper {

    // =========================
    // Entity -> Response DTO
    // =========================
    @Mappings({
            @Mapping(target = "materialId", expression = "java(entity.getMaterial() != null ? entity.getMaterial().getId() : null)"),
            @Mapping(target = "pricePerKg", source = "citizenPricePerKg"),
            @Mapping(target = "effectiveFrom", expression = "java(toLocalDate(entity.getStartDate()))"),
            @Mapping(target = "effectiveTo", expression = "java(toLocalDate(entity.getEndDate()))")
    })
    MaterialPriceResponseDTO toDto(MaterialPrice entity);

    // =========================
    // Entity -> Summary DTO
    // =========================
    @Mappings({
            @Mapping(target = "materialId", expression = "java(entity.getMaterial() != null ? entity.getMaterial().getId() : null)"),
            @Mapping(target = "pricePerKg", source = "citizenPricePerKg"),
            @Mapping(target = "effectiveFrom", expression = "java(toLocalDate(entity.getStartDate()))"),
            @Mapping(target = "effectiveTo", expression = "java(toLocalDate(entity.getEndDate()))")
    })
    MaterialPriceSummaryDTO toSummaryDto(MaterialPrice entity);

    // =========================
    // Request DTO -> Entity
    // =========================
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "citizenPricePerKg", source = "pricePerKg"),
            @Mapping(target = "collectorPricePerKg", source = "pricePerKg"),
            @Mapping(target = "startDate", expression = "java(toStartOfDay(dto.getEffectiveFrom()))"),
            @Mapping(target = "endDate", expression = "java(toEndOfDay(dto.getEffectiveTo()))"),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "material", ignore = true)
    })
    MaterialPrice toEntity(MaterialPriceRequestDTO dto);

    // =========================
    // Update DTO -> existing Entity
    // =========================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "citizenPricePerKg", source = "pricePerKg"),
            @Mapping(target = "collectorPricePerKg", source = "pricePerKg"),
            @Mapping(target = "startDate", ignore = true),
            @Mapping(target = "endDate", expression = "java(dto.getEffectiveTo() != null ? toEndOfDay(dto.getEffectiveTo()) : entity.getEndDate())"),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "material", ignore = true)
    })
    void updateEntityFromDto(MaterialPriceUpdateDTO dto, @MappingTarget MaterialPrice entity);

    default LocalDate toLocalDate(LocalDateTime value) {
        return value != null ? value.toLocalDate() : null;
    }

    default LocalDateTime toStartOfDay(LocalDate value) {
        return value != null ? value.atStartOfDay() : null;
    }

    default LocalDateTime toEndOfDay(LocalDate value) {
        return value != null ? value.atTime(23, 59, 59) : null;
    }
}