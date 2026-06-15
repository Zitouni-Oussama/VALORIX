package com.recyclix.backend.mapper;

import com.recyclix.backend.dto.collector_location_history.CollectorLocationHistoryRequestDTO;
import com.recyclix.backend.dto.collector_location_history.CollectorLocationHistoryResponseDTO;
import com.recyclix.backend.dto.collector_location_history.CollectorLocationHistorySummaryDTO;
import com.recyclix.backend.dto.collector_location_history.CollectorLocationHistoryUpdateDTO;
import com.recyclix.backend.model.CollectorLocationHistory;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CollectorLocationHistoryMapper {

    // =========================
    // Entity -> Response DTO
    // =========================
    @Mapping(target = "collectorId", expression = "java(entity.getCollector() != null ? entity.getCollector().getId() : null)")
    CollectorLocationHistoryResponseDTO toDto(CollectorLocationHistory entity);

    // =========================
    // Entity -> Summary DTO
    // =========================
    @Mapping(target = "collectorId", expression = "java(entity.getCollector() != null ? entity.getCollector().getId() : null)")
    CollectorLocationHistorySummaryDTO toSummaryDto(CollectorLocationHistory entity);

    // =========================
    // Request DTO -> Entity
    // =========================
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "recordedAt", ignore = true),

            // Relation gérée dans le service
            @Mapping(target = "collector", ignore = true)
    })
    CollectorLocationHistory toEntity(CollectorLocationHistoryRequestDTO dto);

    // =========================
    // Update DTO -> existing Entity
    // =========================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "recordedAt", ignore = true),
            @Mapping(target = "collector", ignore = true)
    })
    void updateEntityFromDto(CollectorLocationHistoryUpdateDTO dto, @MappingTarget CollectorLocationHistory entity);
}