package com.recyclix.backend.mapper;

import com.recyclix.backend.dto.ai_classification.AIClassificationRequestDTO;
import com.recyclix.backend.dto.ai_classification.AIClassificationResponseDTO;
import com.recyclix.backend.dto.ai_classification.AIClassificationSummaryDTO;
import com.recyclix.backend.dto.ai_classification.AIClassificationUpdateDTO;
import com.recyclix.backend.model.AIClassification;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface AIClassificationMapper {

    // =========================
    // Entity -> Response DTO
    // =========================
    @Mappings({
            @Mapping(target = "requestId", expression = "java(entity.getRequest() != null ? entity.getRequest().getId() : null)"),
            @Mapping(target = "validatedById", expression = "java(entity.getValidatedBy() != null ? entity.getValidatedBy().getId() : null)")
    })
    AIClassificationResponseDTO toDto(AIClassification entity);

    // =========================
    // Entity -> Summary DTO
    // =========================
    @Mappings({
            @Mapping(target = "requestId", expression = "java(entity.getRequest() != null ? entity.getRequest().getId() : null)")
    })
    AIClassificationSummaryDTO toSummaryDto(AIClassification entity);

    // =========================
    // Request DTO -> Entity
    // =========================
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),

            // Relations gérées dans le service
            @Mapping(target = "request", ignore = true),
            @Mapping(target = "validatedBy", ignore = true)
    })
    AIClassification toEntity(AIClassificationRequestDTO dto);

    // =========================
    // Update DTO -> existing Entity
    // =========================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),

            // request non modifiable
            @Mapping(target = "request", ignore = true),

            // relation gérée dans le service
            @Mapping(target = "validatedBy", ignore = true)
    })
    void updateEntityFromDto(AIClassificationUpdateDTO dto, @MappingTarget AIClassification entity);
}