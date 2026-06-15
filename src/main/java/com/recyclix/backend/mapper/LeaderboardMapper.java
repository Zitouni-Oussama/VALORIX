package com.recyclix.backend.mapper;

import com.recyclix.backend.dto.leaderboard.LeaderboardRequestDTO;
import com.recyclix.backend.dto.leaderboard.LeaderboardResponseDTO;
import com.recyclix.backend.dto.leaderboard.LeaderboardSummaryDTO;
import com.recyclix.backend.dto.leaderboard.LeaderboardUpdateDTO;
import com.recyclix.backend.model.Leaderboard;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface LeaderboardMapper {

    // =========================
    // Entity -> Response DTO
    // =========================
    @Mappings({
            @Mapping(target = "clientId", expression = "java(entity.getClient() != null ? entity.getClient().getId() : null)"),
            @Mapping(target = "period", expression = "java(entity.getPeriodType() != null ? entity.getPeriodType().name() : null)"),
            @Mapping(target = "rank", source = "rankPosition"),
            @Mapping(target = "points", source = "totalPoints")
    })
    LeaderboardResponseDTO toDto(Leaderboard entity);

    // =========================
    // Entity -> Summary DTO
    // =========================
    @Mappings({
            @Mapping(target = "clientId", expression = "java(entity.getClient() != null ? entity.getClient().getId() : null)"),
            @Mapping(target = "period", expression = "java(entity.getPeriodType() != null ? entity.getPeriodType().name() : null)"),
            @Mapping(target = "rank", source = "rankPosition"),
            @Mapping(target = "points", source = "totalPoints")
    })
    LeaderboardSummaryDTO toSummaryDto(Leaderboard entity);

    // =========================
    // Request DTO -> Entity
    // =========================
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "periodType", expression = "java(dto.getPeriod() != null ? com.recyclix.backend.model.Leaderboard.PeriodType.valueOf(dto.getPeriod().toUpperCase()) : null)"),
            @Mapping(target = "periodStart", ignore = true),
            @Mapping(target = "periodEnd", ignore = true),
            @Mapping(target = "totalPoints", source = "points"),
            @Mapping(target = "rankPosition", source = "rank"),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "client", ignore = true)
    })
    Leaderboard toEntity(LeaderboardRequestDTO dto);

    // =========================
    // Update DTO -> existing Entity
    // =========================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "periodType", ignore = true),
            @Mapping(target = "periodStart", ignore = true),
            @Mapping(target = "periodEnd", ignore = true),
            @Mapping(target = "totalPoints", source = "points"),
            @Mapping(target = "rankPosition", source = "rank"),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "client", ignore = true)
    })
    void updateEntityFromDto(LeaderboardUpdateDTO dto, @MappingTarget Leaderboard entity);
}