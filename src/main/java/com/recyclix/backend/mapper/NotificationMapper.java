package com.recyclix.backend.mapper;

import com.recyclix.backend.dto.notification.NotificationRequestDTO;
import com.recyclix.backend.dto.notification.NotificationResponseDTO;
import com.recyclix.backend.dto.notification.NotificationSummaryDTO;
import com.recyclix.backend.dto.notification.NotificationUpdateDTO;
import com.recyclix.backend.model.Notification;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface NotificationMapper {

    // =========================
    // Entity -> Response DTO
    // =========================
    @Mappings({
            @Mapping(target = "accountId", expression = "java(entity.getAccount() != null ? entity.getAccount().getId() : null)"),
            @Mapping(target = "type", expression = "java(entity.getType() != null ? entity.getType().name() : null)"),
            @Mapping(target = "targetRole", expression = "java(entity.getTargetRole() != null ? entity.getTargetRole().name() : null)")
    })
    NotificationResponseDTO toDto(Notification entity);

    // =========================
    // Entity -> Summary DTO
    // =========================
    @Mapping(target = "type", expression = "java(entity.getType() != null ? entity.getType().name() : null)")
    NotificationSummaryDTO toSummaryDto(Notification entity);

    // =========================
    // Request DTO -> Entity
    // =========================
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "type", expression = "java(dto.getType() != null ? com.recyclix.backend.model.Notification.NotificationType.valueOf(dto.getType().toUpperCase()) : null)"),
            @Mapping(target = "isRead", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "account", ignore = true)
    })
    Notification toEntity(NotificationRequestDTO dto);

    // =========================
    // Update DTO -> existing Entity
    // =========================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "title", ignore = true),
            @Mapping(target = "message", ignore = true),
            @Mapping(target = "type", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "account", ignore = true)
    })
    void updateEntityFromDto(NotificationUpdateDTO dto, @MappingTarget Notification entity);
}