// com/recyclix/backend/mapper/FactoryUserMapper.java
package com.recyclix.backend.mapper;

import com.recyclix.backend.dto.factory_user.FactoryUserRequestDTO;
import com.recyclix.backend.dto.factory_user.FactoryUserResponseDTO;
import com.recyclix.backend.dto.factory_user.FactoryUserSummaryDTO;
import com.recyclix.backend.dto.factory_user.FactoryUserUpdateDTO;
import com.recyclix.backend.model.FactoryUser;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface FactoryUserMapper {

    // =========================
    // Entity -> Response DTO
    // =========================
    @Mappings({
            @Mapping(target = "accountId", source = "account.id"),
            @Mapping(target = "validationsCount", ignore = true),
            @Mapping(target = "expensesCount", ignore = true),
            @Mapping(target = "financialReportsCount", ignore = true),
            @Mapping(target = "recordedAbsencesCount", ignore = true),
            @Mapping(target = "reportedIncidentsCount", ignore = true),
            @Mapping(target = "aiClassificationsCount", ignore = true),
            @Mapping(target = "faqEntriesCount", ignore = true),
            @Mapping(target = "supportTicketsCount", ignore = true)
    })
    FactoryUserResponseDTO toDto(FactoryUser entity);

    // =========================
    // Entity -> Summary DTO
    // =========================
    FactoryUserSummaryDTO toSummaryDto(FactoryUser entity);

    // =========================
    // Request DTO -> Entity
    // =========================
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "account", ignore = true),
            @Mapping(target = "validations", ignore = true),
            @Mapping(target = "expenses", ignore = true),
            @Mapping(target = "financialReports", ignore = true),
            @Mapping(target = "recordedAbsences", ignore = true),
            @Mapping(target = "reportedIncidents", ignore = true),
            @Mapping(target = "aiClassifications", ignore = true),
            @Mapping(target = "faqEntries", ignore = true),
            @Mapping(target = "supportTickets", ignore = true),
            @Mapping(target = "recyclingCenter", ignore = true)
    })
    FactoryUser toEntity(FactoryUserRequestDTO dto);

    // =========================
    // Update DTO -> existing Entity
    // =========================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "account", ignore = true),
            @Mapping(target = "validations", ignore = true),
            @Mapping(target = "expenses", ignore = true),
            @Mapping(target = "financialReports", ignore = true),
            @Mapping(target = "recordedAbsences", ignore = true),
            @Mapping(target = "reportedIncidents", ignore = true),
            @Mapping(target = "aiClassifications", ignore = true),
            @Mapping(target = "faqEntries", ignore = true),
            @Mapping(target = "supportTickets", ignore = true),
            @Mapping(target = "recyclingCenter", ignore = true)
    })
    void updateEntityFromDto(FactoryUserUpdateDTO dto, @MappingTarget FactoryUser entity);
}