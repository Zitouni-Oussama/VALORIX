package com.recyclix.backend.mapper;

import com.recyclix.backend.dto.collection.CollectionRequestDTO;
import com.recyclix.backend.dto.collection.CollectionResponseDTO;
import com.recyclix.backend.dto.collection.CollectionSummaryDTO;
import com.recyclix.backend.dto.collection.CollectionUpdateDTO;
import com.recyclix.backend.model.Collection;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CollectionMapper {

    // =========================
    // Entity -> Response DTO
    // =========================
    @Mappings({
            @Mapping(target = "requestId", expression = "java(entity.getRequest() != null ? entity.getRequest().getId() : null)"),
            @Mapping(target = "collectorId", expression = "java(entity.getCollector() != null ? entity.getCollector().getId() : null)"),
            @Mapping(target = "factoryDeliveryId", expression = "java(entity.getFactoryDelivery() != null ? entity.getFactoryDelivery().getId() : null)"),
            @Mapping(target = "transactionId", expression = "java(entity.getTransaction() != null ? entity.getTransaction().getId() : null)"),
            @Mapping(target = "pointMovementId", expression = "java(entity.getPointMovement() != null ? entity.getPointMovement().getId() : null)"),
            @Mapping(target = "rating", source = "rating"),
            @Mapping(target = "feedbackComment", source = "feedbackComment"),
            @Mapping(target = "ratedAt", source = "ratedAt")
    })
    CollectionResponseDTO toDto(Collection entity);

    // =========================
    // Entity -> Summary DTO
    // =========================
    @Mappings({
            @Mapping(target = "requestId", expression = "java(entity.getRequest() != null ? entity.getRequest().getId() : null)"),
            @Mapping(target = "collectorId", expression = "java(entity.getCollector() != null ? entity.getCollector().getId() : null)"),
            @Mapping(target = "rating", source = "rating")
    })
    CollectionSummaryDTO toSummaryDto(Collection entity);

    // =========================
    // Request DTO -> Entity
    // =========================
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "collectedAt", ignore = true),
            @Mapping(target = "createdAt", ignore = true),

            // Relations gérées dans le service
            @Mapping(target = "request", ignore = true),
            @Mapping(target = "collector", ignore = true),
            @Mapping(target = "factoryDelivery", ignore = true),
            @Mapping(target = "transaction", ignore = true),
            @Mapping(target = "pointMovement", ignore = true)
    })
    Collection toEntity(CollectionRequestDTO dto);

    // =========================
    // Update DTO -> existing Entity
    // =========================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "collectedAt", ignore = true),
            @Mapping(target = "createdAt", ignore = true),

            @Mapping(target = "request", ignore = true),
            @Mapping(target = "collector", ignore = true),
            @Mapping(target = "factoryDelivery", ignore = true),
            @Mapping(target = "transaction", ignore = true),
            @Mapping(target = "pointMovement", ignore = true)
    })
    void updateEntityFromDto(CollectionUpdateDTO dto, @MappingTarget Collection entity);
}