package com.recyclix.backend.mapper;

import com.recyclix.backend.dto.point_movement.PointMovementRequestDTO;
import com.recyclix.backend.dto.point_movement.PointMovementResponseDTO;
import com.recyclix.backend.dto.point_movement.PointMovementSummaryDTO;
import com.recyclix.backend.dto.point_movement.PointMovementUpdateDTO;
import com.recyclix.backend.model.PointMovement;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PointMovementMapper {

    // =========================
    // Entity -> Response DTO
    // =========================
    @Mappings({
            @Mapping(target = "accountId", expression = "java(entity.getAccount() != null ? entity.getAccount().getId() : null)"),
            @Mapping(target = "collectionId", expression = "java(entity.getCollection() != null ? entity.getCollection().getId() : null)"),
            @Mapping(target = "type", expression = "java(entity.getType() != null ? entity.getType().name() : null)"),
            @Mapping(target = "points", source = "pointsAmount"),
            @Mapping(target = "description", ignore = true)
    })
    PointMovementResponseDTO toResponseDTO(PointMovement entity);

    // =========================
    // Entity -> Summary DTO
    // =========================
    @Mappings({
            @Mapping(target = "type", expression = "java(entity.getType() != null ? entity.getType().name() : null)"),
            @Mapping(target = "points", source = "pointsAmount")
    })
    PointMovementSummaryDTO toSummaryDTO(PointMovement entity);

    // =========================
    // Request DTO -> Entity
    // =========================
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "account", ignore = true),
            @Mapping(target = "collection", ignore = true),
            @Mapping(target = "pointsAmount", source = "points"),
            @Mapping(target = "type", expression = "java(mapType(dto.getType()))")
    })
    PointMovement toEntity(PointMovementRequestDTO dto);

    // =========================
    // Update DTO -> Entity existante
    // =========================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "account", ignore = true),
            @Mapping(target = "collection", ignore = true),
            @Mapping(target = "type", ignore = true),
            @Mapping(target = "pointsAmount", ignore = true)
    })
    void updateEntityFromDTO(PointMovementUpdateDTO dto, @MappingTarget PointMovement entity);

    // =========================
    // Helper
    // =========================
    default PointMovement.PointMovementType mapType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return PointMovement.PointMovementType.valueOf(value.trim().toUpperCase());
    }
}