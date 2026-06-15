package com.recyclix.backend.mapper;

import com.recyclix.backend.dto.challenge.ChallengeRequestDTO;
import com.recyclix.backend.dto.challenge.ChallengeResponseDTO;
import com.recyclix.backend.dto.challenge.ChallengeSummaryDTO;
import com.recyclix.backend.dto.challenge.ChallengeUpdateDTO;
import com.recyclix.backend.model.Challenge;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ChallengeMapper {

    @Mapping(
            target = "userChallengesCount",
            expression = "java(entity.getUserChallenges() != null ? entity.getUserChallenges().size() : 0)"
    )
    ChallengeResponseDTO toDto(Challenge entity);

    ChallengeSummaryDTO toSummaryDto(Challenge entity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "userChallenges", ignore = true)
    })
    Challenge toEntity(ChallengeRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "userChallenges", ignore = true)
    })
    void updateEntityFromDto(ChallengeUpdateDTO dto, @MappingTarget Challenge entity);
}