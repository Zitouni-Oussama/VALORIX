// recyclix\backend\mapper\FaqEntryMapper.java
package com.recyclix.backend.mapper;

import com.recyclix.backend.dto.faq_entry.FaqEntryRequestDTO;
import com.recyclix.backend.dto.faq_entry.FaqEntryResponseDTO;
import com.recyclix.backend.dto.faq_entry.FaqEntrySummaryDTO;
import com.recyclix.backend.dto.faq_entry.FaqEntryUpdateDTO;
import com.recyclix.backend.model.FaqEntry;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface FaqEntryMapper {

    // =========================
    // Entity -> Response DTO
    // =========================
    @Mappings({
            @Mapping(target = "createdById",
                    expression = "java(entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null)")
            // roleType, categoryKey, categoryLabel, displayOrder, status sont mappés automatiquement
    })
    FaqEntryResponseDTO toDto(FaqEntry entity);

    // =========================
    // Entity -> Summary DTO
    // =========================
    @Mappings({
            @Mapping(target = "createdById",
                    expression = "java(entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null)")
    })
    FaqEntrySummaryDTO toSummaryDto(FaqEntry entity);

    // =========================
    // Request DTO -> Entity
    // =========================
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "roleType", source = "roleType"),        // ✅ Depuis le DTO
            @Mapping(target = "categoryKey", source = "categoryKey"),  // ✅ Depuis le DTO
            @Mapping(target = "categoryLabel", source = "categoryLabel"), // ✅ Depuis le DTO
            @Mapping(target = "displayOrder", source = "displayOrder",
                    defaultValue = "0"),                               // ✅ Valeur par défaut
            @Mapping(target = "status", source = "status",
                    defaultValue = "ACTIVE"),                          // ✅ Valeur par défaut
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "createdBy", ignore = true)
    })
    FaqEntry toEntity(FaqEntryRequestDTO dto);

    // =========================
    // Update DTO -> existing Entity
    // =========================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "roleType", source = "roleType"),         // ✅ Modifiable
            @Mapping(target = "categoryKey", source = "categoryKey"),   // ✅ Modifiable
            @Mapping(target = "categoryLabel", source = "categoryLabel"), // ✅ Modifiable
            @Mapping(target = "displayOrder", source = "displayOrder"), // ✅ Modifiable
            @Mapping(target = "status", source = "status"),             // ✅ Modifiable
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "createdBy", ignore = true)
    })
    void updateEntityFromDto(FaqEntryUpdateDTO dto, @MappingTarget FaqEntry entity);
}