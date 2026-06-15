package com.recyclix.backend.mapper;

import com.recyclix.backend.dto.truck.TruckRequestDTO;
import com.recyclix.backend.dto.truck.TruckResponseDTO;
import com.recyclix.backend.dto.truck.TruckSummaryDTO;
import com.recyclix.backend.dto.truck.TruckUpdateDTO;
import com.recyclix.backend.model.Truck;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TruckMapper {

    // =========================
    // Entity -> Response DTO
    // =========================
    @Mappings({
            @Mapping(target = "collectorId", expression = "java(entity.getCollector() != null ? entity.getCollector().getId() : null)"),
            @Mapping(target = "capacityKg", source = "capacityKg"),   // ✅ double -> double
            @Mapping(target = "collectionsCount", ignore = true),
            @Mapping(target = "status", expression = "java(entity.getIsActive() != null && entity.getIsActive() ? \"ACTIVE\" : \"INACTIVE\")")
    })
    TruckResponseDTO toResponseDTO(Truck entity);

    // =========================
    // Entity -> Summary DTO
    // =========================
    @Mappings({
            @Mapping(target = "capacityKg", source = "capacityKg"),   // ✅
            @Mapping(target = "status", expression = "java(entity.getIsActive() != null && entity.getIsActive() ? \"ACTIVE\" : \"INACTIVE\")")
    })
    TruckSummaryDTO toSummaryDTO(Truck entity);

    // =========================
    // Request DTO -> Entity
    // =========================
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "collector", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "brand", ignore = true),        // géré manuellement dans le service
            @Mapping(target = "truckPhotoUrl", ignore = true),
            @Mapping(target = "greyCardImageUrl", ignore = true),
            @Mapping(target = "drivingLicenseImageUrl", ignore = true),
            @Mapping(target = "capacityKg", source = "capacityKg"), // ✅ BigDecimal -> double
            @Mapping(target = "isActive", expression = "java(mapStatus(dto.getStatus()))")
    })
    Truck toEntity(TruckRequestDTO dto);

    // =========================
    // Update DTO -> Entity existante
    // =========================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "collector", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "brand", ignore = true),
            @Mapping(target = "truckPhotoUrl", ignore = true),
            @Mapping(target = "greyCardImageUrl", ignore = true),
            @Mapping(target = "drivingLicenseImageUrl", ignore = true),
            @Mapping(target = "capacityKg", source = "capacityKg", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE), // ✅
            @Mapping(target = "isActive", expression = "java(dto.getStatus() != null ? mapStatus(dto.getStatus()) : entity.getIsActive())")
    })
    void updateEntityFromDTO(TruckUpdateDTO dto, @MappingTarget Truck entity);

    // =========================
    // Helper
    // =========================
    default Boolean mapStatus(String value) {
        if (value == null || value.isBlank()) return null;
        return switch (value.trim().toUpperCase()) {
            case "ACTIVE", "ACTIF", "EN_SERVICE", "AVAILABLE" -> true;
            case "INACTIVE", "INACTIF", "OUT_OF_SERVICE", "DISABLED" -> false;
            default -> null;
        };
    }
}