package com.recyclix.backend.mapper;

import com.recyclix.backend.dto.support_ticket.SupportTicketRequestDTO;
import com.recyclix.backend.dto.support_ticket.SupportTicketResponseDTO;
import com.recyclix.backend.dto.support_ticket.SupportTicketSummaryDTO;
import com.recyclix.backend.dto.support_ticket.SupportTicketUpdateDTO;
import com.recyclix.backend.model.SupportTicket;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface SupportTicketMapper {

    // =========================
    // Entity -> Response DTO
    // =========================
    @Mappings({
            @Mapping(target = "accountId", expression = "java(entity.getAccount() != null ? entity.getAccount().getId() : null)"),
            @Mapping(target = "assignedToId", expression = "java(entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null)"),
            @Mapping(target = "description", source = "message"),
            @Mapping(target = "status", expression = "java(entity.getStatus() != null ? entity.getStatus().name() : null)"),
            @Mapping(target = "priority", ignore = true)
    })
    SupportTicketResponseDTO toResponseDTO(SupportTicket entity);

    // =========================
    // Entity -> Summary DTO
    // =========================
    @Mappings({
            @Mapping(target = "status", expression = "java(entity.getStatus() != null ? entity.getStatus().name() : null)"),
            @Mapping(target = "priority", ignore = true)
    })
    SupportTicketSummaryDTO toSummaryDTO(SupportTicket entity);

    // =========================
    // Request DTO -> Entity
    // =========================
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "account", ignore = true),
            @Mapping(target = "createdBy", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "respondedAt", ignore = true),
            @Mapping(target = "responseMessage", ignore = true),
            @Mapping(target = "message", source = "description"),
            @Mapping(target = "status", ignore = true),   // OPEN par défaut via @PrePersist
            @Mapping(target = "roleType", ignore = true)  // obligatoire dans le modèle, à fixer dans le service
    })
    SupportTicket toEntity(SupportTicketRequestDTO dto);

    // =========================
    // Update DTO -> Entity existante
    // =========================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "account", ignore = true),
            @Mapping(target = "createdBy", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "respondedAt", ignore = true),
            @Mapping(target = "responseMessage", ignore = true),
            @Mapping(target = "roleType", ignore = true),
            @Mapping(target = "subject", ignore = true),
            @Mapping(target = "message", ignore = true),
            @Mapping(target = "status", expression = "java(dto.getStatus() != null ? mapStatus(dto.getStatus()) : entity.getStatus())")
    })
    void updateEntityFromDTO(SupportTicketUpdateDTO dto, @MappingTarget SupportTicket entity);

    // =========================
    // Helper
    // =========================
    default SupportTicket.Status mapStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return SupportTicket.Status.valueOf(value.trim().toUpperCase());
    }

}