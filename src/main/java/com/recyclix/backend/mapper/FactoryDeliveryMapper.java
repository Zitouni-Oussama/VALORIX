package com.recyclix.backend.mapper;

import com.recyclix.backend.dto.factory_delivery.FactoryDeliveryRequestDTO;
import com.recyclix.backend.dto.factory_delivery.FactoryDeliveryResponseDTO;
import com.recyclix.backend.dto.factory_delivery.FactoryDeliverySummaryDTO;
import com.recyclix.backend.dto.factory_delivery.FactoryDeliveryUpdateDTO;
import com.recyclix.backend.model.FactoryDelivery;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface FactoryDeliveryMapper {

    // =========================
    // Entity -> Response DTO
    // =========================
    @Mappings({
            @Mapping(target = "collectionId", expression = "java(entity.getCollection() != null ? entity.getCollection().getId() : null)"),
            @Mapping(target = "validationId", expression = "java(entity.getValidation() != null ? entity.getValidation().getId() : null)"),
            @Mapping(target = "recyclingCenterId", expression = "java(entity.getRecyclingCenter() != null ? entity.getRecyclingCenter().getId() : null)")
    })
    FactoryDeliveryResponseDTO toDto(FactoryDelivery entity);

    // =========================
    // Entity -> Summary DTO
    // =========================
    @Mapping(target = "collectionId", expression = "java(entity.getCollection() != null ? entity.getCollection().getId() : null)")
    FactoryDeliverySummaryDTO toSummaryDto(FactoryDelivery entity);

    // =========================
    // Request DTO -> Entity
    // =========================
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),

            // Relations gérées dans le service
            @Mapping(target = "collection", ignore = true),
            @Mapping(target = "validation", ignore = true),
            @Mapping(target = "recyclingCenter", ignore = true)
    })
    FactoryDelivery toEntity(FactoryDeliveryRequestDTO dto);

    // =========================
    // Update DTO -> existing Entity
    // =========================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),

            @Mapping(target = "collection", ignore = true),
            @Mapping(target = "validation", ignore = true),
            @Mapping(target = "recyclingCenter", ignore = true)
    })
    void updateEntityFromDto(FactoryDeliveryUpdateDTO dto, @MappingTarget FactoryDelivery entity);
}