package com.recyclix.backend.mapper;

import com.recyclix.backend.dto.collection_request.CollectionRequestRequestDTO;
import com.recyclix.backend.dto.collection_request.CollectionRequestResponseDTO;
import com.recyclix.backend.dto.collection_request.CollectionRequestSummaryDTO;
import com.recyclix.backend.dto.collection_request.CollectionRequestUpdateDTO;
import com.recyclix.backend.model.CollectionRequest;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CollectionRequestMapper {

    // =========================
    // Entity -> Response DTO
    // =========================
    @Mappings({
            @Mapping(target = "clientId", expression = "java(entity.getClient() != null ? entity.getClient().getId() : null)"),
            @Mapping(target = "materialId", expression = "java(entity.getMaterial() != null ? entity.getMaterial().getId() : null)"),
            @Mapping(target = "aiClassificationId", expression = "java(entity.getAiClassification() != null ? entity.getAiClassification().getId() : null)"),
            @Mapping(target = "collectionId", expression = "java(entity.getCollection() != null ? entity.getCollection().getId() : null)")
    })
    CollectionRequestResponseDTO toDto(CollectionRequest entity);

    // =========================
    // Entity -> Summary DTO
    // =========================
    @Mappings({
            @Mapping(target = "clientId", expression = "java(entity.getClient() != null ? entity.getClient().getId() : null)"),
            @Mapping(target = "materialId", expression = "java(entity.getMaterial() != null ? entity.getMaterial().getId() : null)")
    })
    CollectionRequestSummaryDTO toSummaryDto(CollectionRequest entity);

    // =========================
    // Request DTO -> Entity
    // =========================
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),

            // Relations gérées dans le service
            @Mapping(target = "client", ignore = true),
            @Mapping(target = "material", ignore = true),
            @Mapping(target = "aiClassification", ignore = true),
            @Mapping(target = "collection", ignore = true)
    })
    CollectionRequest toEntity(CollectionRequestRequestDTO dto);

    // =========================
    // Update DTO -> existing Entity
    // =========================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),

            @Mapping(target = "client", ignore = true),
            @Mapping(target = "material", ignore = true),
            @Mapping(target = "aiClassification", ignore = true),
            @Mapping(target = "collection", ignore = true)
    })
    void updateEntityFromDto(CollectionRequestUpdateDTO dto, @MappingTarget CollectionRequest entity);
}