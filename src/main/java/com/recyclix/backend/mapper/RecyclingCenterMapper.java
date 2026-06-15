// java/com/recyclix/backend/mapper/RecyclingCenterMapper.java
package com.recyclix.backend.mapper;

import com.recyclix.backend.dto.recycling_center.RecyclingCenterRequestDTO;
import com.recyclix.backend.dto.recycling_center.RecyclingCenterResponseDTO;
import com.recyclix.backend.dto.recycling_center.RecyclingCenterSummaryDTO;
import com.recyclix.backend.dto.recycling_center.RecyclingCenterUpdateDTO;
import com.recyclix.backend.model.RecyclingCenter;
import org.mapstruct.*;
import org.mapstruct.Named;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface RecyclingCenterMapper {

    // =========================
    // Entity -> Response DTO
    // =========================
    @Mappings({
            @Mapping(target = "address", source = "location"),
            @Mapping(target = "latitude", ignore = true),
            @Mapping(target = "longitude", ignore = true),
            @Mapping(target = "contactEmail", source = "contactInfo", qualifiedByName = "extractEmail"),
            @Mapping(target = "contactPhone", source = "contactInfo", qualifiedByName = "extractPhone"),
            @Mapping(
                    target = "deliveries",
                    expression = "java(entity.getDeliveries() != null ? entity.getDeliveries().size() : 0)"
            )
    })
    RecyclingCenterResponseDTO toResponseDTO(RecyclingCenter entity);

    // =========================
    // Entity -> Summary DTO
    // =========================
    @Mappings({
            @Mapping(target = "address", source = "location"),
            @Mapping(target = "contactEmail", source = "contactInfo", qualifiedByName = "extractEmail"),
            @Mapping(target = "contactPhone", source = "contactInfo", qualifiedByName = "extractPhone")
    })
    RecyclingCenterSummaryDTO toSummaryDTO(RecyclingCenter entity);

    // =========================
    // Request DTO -> Entity
    // =========================
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "deliveries", ignore = true),
            @Mapping(target = "location", source = "address"),
            @Mapping(target = "contactInfo", expression = "java(mergeContactInfo(dto.getContactEmail(), dto.getContactPhone()))"),
            @Mapping(target = "capacity", source = "capacity")
    })
    RecyclingCenter toEntity(RecyclingCenterRequestDTO dto);

    // =========================
    // Update DTO -> Entity existante
    // =========================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "deliveries", ignore = true),
            @Mapping(target = "capacity", source = "capacity"),
            @Mapping(target = "location", source = "address"),
            @Mapping(target = "contactInfo", expression = "java(resolveUpdatedContactInfo(dto, entity))")
    })
    void updateEntityFromDTO(RecyclingCenterUpdateDTO dto, @MappingTarget RecyclingCenter entity);

    // =========================
    // Helpers d'extraction avec @Named
    // =========================

    @Named("extractEmail")
    default String extractEmail(String contactInfo) {
        if (contactInfo == null || contactInfo.isBlank()) return null;
        if (contactInfo.contains("Email:")) {
            int start = contactInfo.indexOf("Email:") + 6;
            int end = contactInfo.indexOf("|");
            if (end > start) {
                return contactInfo.substring(start, end).trim();
            }
            return contactInfo.substring(start).trim();
        }
        return null;
    }

    @Named("extractPhone")
    default String extractPhone(String contactInfo) {
        if (contactInfo == null || contactInfo.isBlank()) return null;
        if (contactInfo.contains("Phone:")) {
            int start = contactInfo.indexOf("Phone:") + 6;
            return contactInfo.substring(start).trim();
        }
        return null;
    }

    default String mergeContactInfo(String email, String phone) {
        if ((email == null || email.isBlank()) && (phone == null || phone.isBlank())) {
            return null;
        }
        if (email != null && !email.isBlank() && phone != null && !phone.isBlank()) {
            return "Email: " + email + " | Phone: " + phone;
        }
        if (email != null && !email.isBlank()) {
            return "Email: " + email;
        }
        return "Phone: " + phone;
    }

    default String resolveUpdatedContactInfo(RecyclingCenterUpdateDTO dto, RecyclingCenter entity) {
        boolean hasEmail = dto.getContactEmail() != null && !dto.getContactEmail().isBlank();
        boolean hasPhone = dto.getContactPhone() != null && !dto.getContactPhone().isBlank();

        if (!hasEmail && !hasPhone) {
            return entity.getContactInfo();
        }
        return mergeContactInfo(dto.getContactEmail(), dto.getContactPhone());
    }
}