package com.recyclix.backend.mapper;

import com.recyclix.backend.dto.collector.CollectorRequestDTO;
import com.recyclix.backend.dto.collector.CollectorResponseDTO;
import com.recyclix.backend.dto.collector.CollectorSummaryDTO;
import com.recyclix.backend.dto.collector.CollectorUpdateDTO;
import com.recyclix.backend.model.Collector;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CollectorMapper {

    // =========================
    // Entity -> Response DTO
    // =========================
    @Mappings({
            @Mapping(target = "accountId", expression = "java(entity.getAccount() != null ? entity.getAccount().getId() : null)"),
            @Mapping(target = "truckId", expression = "java(entity.getTruck() != null ? entity.getTruck().getId() : null)"),
            @Mapping(target = "collectorLocationHistoryId", expression = "java(entity.getCollectorLocationHistory() != null ? entity.getCollectorLocationHistory().getId() : null)"),
            @Mapping(target = "collectionsCount", expression = "java(entity.getCollections() != null ? entity.getCollections().size() : 0)"),
            @Mapping(target = "accountStatus", expression = "java(entity.getAccount().getStatus().name())")
    })
    CollectorResponseDTO toDto(Collector entity);

    // =========================
    // Entity -> Summary DTO
    // =========================
    @Mapping(target = "accountId", expression = "java(entity.getAccount() != null ? entity.getAccount().getId() : null)")
    CollectorSummaryDTO toSummaryDto(Collector entity);

    // =========================
    // Request DTO -> Entity
    // =========================
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),

            // Relations gérées dans le service
            @Mapping(target = "account", ignore = true),
            @Mapping(target = "truck", ignore = true),
            @Mapping(target = "collectorLocationHistory", ignore = true),
            @Mapping(target = "collections", ignore = true)
    })
    Collector toEntity(CollectorRequestDTO dto);

    // =========================
    // Update DTO -> existing Entity
    // =========================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),

            @Mapping(target = "account", ignore = true),
            @Mapping(target = "truck", ignore = true),
            @Mapping(target = "collectorLocationHistory", ignore = true),
            @Mapping(target = "collections", ignore = true)
    })
    void updateEntityFromDto(CollectorUpdateDTO dto, @MappingTarget Collector entity);
}