package com.recyclix.backend.mapper;

import com.recyclix.backend.dto.factory_validation.FactoryValidationRequestDTO;
import com.recyclix.backend.dto.factory_validation.FactoryValidationResponseDTO;
import com.recyclix.backend.dto.factory_validation.FactoryValidationSummaryDTO;
import com.recyclix.backend.dto.factory_validation.FactoryValidationUpdateDTO;
import com.recyclix.backend.model.FactoryValidation;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface FactoryValidationMapper {

    // =========================
    // Entity -> Response DTO
    // =========================
    @Mappings({
            @Mapping(target = "deliveryId", expression = "java(entity.getDelivery() != null ? entity.getDelivery().getId() : null)"),
            @Mapping(target = "validatedById", expression = "java(entity.getValidatedBy() != null ? entity.getValidatedBy().getId() : null)")
    })
    FactoryValidationResponseDTO toDto(FactoryValidation entity);

    // =========================
    // Entity -> Summary DTO
    // =========================
    @Mappings({
            @Mapping(target = "deliveryId", expression = "java(entity.getDelivery() != null ? entity.getDelivery().getId() : null)"),
            @Mapping(target = "validatedById", expression = "java(entity.getValidatedBy() != null ? entity.getValidatedBy().getId() : null)")
    })
    FactoryValidationSummaryDTO toSummaryDto(FactoryValidation entity);

    // =========================
    // Request DTO -> Entity
    // =========================
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "validatedAt", ignore = true),

            // Relations gérées dans le service
            @Mapping(target = "delivery", ignore = true),
            @Mapping(target = "validatedBy", ignore = true)
    })
    FactoryValidation toEntity(FactoryValidationRequestDTO dto);

    // =========================
    // Update DTO -> existing Entity
    // =========================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "validatedAt", ignore = true),

            @Mapping(target = "delivery", ignore = true),
            @Mapping(target = "validatedBy", ignore = true)
    })
    void updateEntityFromDto(FactoryValidationUpdateDTO dto, @MappingTarget FactoryValidation entity);
}