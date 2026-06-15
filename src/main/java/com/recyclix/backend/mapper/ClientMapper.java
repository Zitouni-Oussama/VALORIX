package com.recyclix.backend.mapper;

import com.recyclix.backend.dto.client.ClientRequestDTO;
import com.recyclix.backend.dto.client.ClientResponseDTO;
import com.recyclix.backend.dto.client.ClientSummaryDTO;
import com.recyclix.backend.dto.client.ClientUpdateDTO;
import com.recyclix.backend.model.Client;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ClientMapper {

    // =========================
    // Entity -> Response DTO
    // =========================
    @Mappings({
            @Mapping(target = "accountId", expression = "java(entity.getAccount() != null ? entity.getAccount().getId() : null)"),
            @Mapping(target = "collectionRequestsCount", expression = "java(entity.getCollectionRequests() != null ? entity.getCollectionRequests().size() : 0)")
    })
    ClientResponseDTO toDto(Client entity);

    // =========================
    // Entity -> Summary DTO
    // =========================
    @Mapping(target = "accountId", expression = "java(entity.getAccount() != null ? entity.getAccount().getId() : null)")
    ClientSummaryDTO toSummaryDto(Client entity);

    // =========================
    // Request DTO -> Entity
    // =========================
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),

            // Relations gérées dans le service
            @Mapping(target = "account", ignore = true),
            @Mapping(target = "collectionRequests", ignore = true),
            @Mapping(target = "leaderboards", ignore = true)
    })
    Client toEntity(ClientRequestDTO dto);

    // =========================
    // Update DTO -> existing Entity
    // =========================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),

            @Mapping(target = "account", ignore = true),
            @Mapping(target = "collectionRequests", ignore = true),
            @Mapping(target = "leaderboards", ignore = true)
    })
    void updateEntityFromDto(ClientUpdateDTO dto, @MappingTarget Client entity);
}