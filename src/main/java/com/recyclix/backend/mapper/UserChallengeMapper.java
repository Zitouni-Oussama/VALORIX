package com.recyclix.backend.mapper;

import com.recyclix.backend.dto.user_challenge.*;
import com.recyclix.backend.model.UserChallenge;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserChallengeMapper {

    @Mappings({
            @Mapping(target = "accountId",
                    expression = "java(entity.getAccount() != null ? entity.getAccount().getId() : null)"),
            @Mapping(target = "challengeId",
                    expression = "java(entity.getChallenge() != null ? entity.getChallenge().getId() : null)"),
            @Mapping(target = "progress", source = "progressQuantity"),
            @Mapping(target = "status",
                    expression = "java(entity.getStatus() != null ? entity.getStatus().name() : null)"),
            @Mapping(target = "challengeTitle", expression = "java(entity.getChallenge() != null ? entity.getChallenge().getTitle() : null)"),
            @Mapping(target = "challengeDescription", expression = "java(entity.getChallenge() != null ? entity.getChallenge().getDescription() : null)"),
            @Mapping(
                    target = "targetValue",
                    expression = "java(entity.getChallenge() != null && entity.getChallenge().getTargetValue() != null ? Double.valueOf(entity.getChallenge().getTargetValue()) : null)"
            )
    })
    UserChallengeResponseDTO toResponseDTO(UserChallenge entity);

    @Mappings({
            @Mapping(target = "challengeId",
                    expression = "java(entity.getChallenge() != null ? entity.getChallenge().getId() : null)"),
            @Mapping(target = "status",
                    expression = "java(entity.getStatus() != null ? entity.getStatus().name() : null)"),
            @Mapping(target = "challengeTitle", expression = "java(entity.getChallenge() != null ? entity.getChallenge().getTitle() : null)"),
            @Mapping(target = "challengeDescription", expression = "java(entity.getChallenge() != null ? entity.getChallenge().getDescription() : null)"),
            @Mapping(
                    target = "targetValue",
                    expression = "java(entity.getChallenge() != null && entity.getChallenge().getTargetValue() != null ? Double.valueOf(entity.getChallenge().getTargetValue()) : null)"
            )
    })
    UserChallengeSummaryDTO toSummaryDTO(UserChallenge entity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "account", ignore = true),
            @Mapping(target = "challenge", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "completedAt", ignore = true),
            @Mapping(target = "progressQuantity", ignore = true),
            @Mapping(target = "status", ignore = true)
    })
    UserChallenge toEntity(UserChallengeRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "account", ignore = true),
            @Mapping(target = "challenge", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "progressQuantity", source = "progress"),
            @Mapping(target = "status",
                    expression = "java(dto.getStatus() != null ? mapStatus(dto.getStatus()) : entity.getStatus())")
    })
    void updateEntityFromDTO(UserChallengeUpdateDTO dto, @MappingTarget UserChallenge entity);

    default UserChallenge.ChallengeStatus mapStatus(String value) {
        if (value == null || value.isBlank()) return null;
        return UserChallenge.ChallengeStatus.valueOf(value.trim().toUpperCase());
    }
}