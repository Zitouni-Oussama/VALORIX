package com.recyclix.backend.mapper;

import com.recyclix.backend.dto.transaction.TransactionRequestDTO;
import com.recyclix.backend.dto.transaction.TransactionResponseDTO;
import com.recyclix.backend.dto.transaction.TransactionSummaryDTO;
import com.recyclix.backend.dto.transaction.TransactionUpdateDTO;
import com.recyclix.backend.model.Transaction;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TransactionMapper {

    // =========================
    // Entity -> Response DTO
    // =========================
    @Mappings({
            @Mapping(target = "accountId", expression = "java(entity.getAccount() != null ? entity.getAccount().getId() : null)"),
            @Mapping(target = "collectionId", expression = "java(entity.getCollection() != null ? entity.getCollection().getId() : null)"),
            @Mapping(target = "type", expression = "java(entity.getType() != null ? entity.getType().name() : null)"),
            @Mapping(target = "paymentId", ignore = true),
            @Mapping(target = "description", ignore = true),
            @Mapping(target = "accountEmail", expression = "java(entity.getAccount() != null ? entity.getAccount().getEmail() : null)"),
            @Mapping(target = "accountHolderName", source = "accountHolderName")
    })
    TransactionResponseDTO toResponseDTO(Transaction entity);

    // =========================
    // Entity -> Summary DTO
    // =========================
    @Mappings({
            @Mapping(target = "type", expression = "java(entity.getType() != null ? entity.getType().name() : null)")
    })
    TransactionSummaryDTO toSummaryDTO(Transaction entity);

    // =========================
    // Request DTO -> Entity
    // =========================
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "status", ignore = true), // PENDING par défaut via @PrePersist
            @Mapping(target = "account", ignore = true),
            @Mapping(target = "collection", ignore = true),
            @Mapping(target = "type", expression = "java(mapType(dto.getType()))")
    })
    Transaction toEntity(TransactionRequestDTO dto);

    // =========================
    // Update DTO -> Entity existante
    // =========================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "status", ignore = true),
            @Mapping(target = "account", ignore = true),
            @Mapping(target = "collection", ignore = true),
            @Mapping(target = "type", ignore = true),
            @Mapping(target = "amount", ignore = true)
    })
    void updateEntityFromDTO(TransactionUpdateDTO dto, @MappingTarget Transaction entity);

    // =========================
    // Helper
    // =========================
    default Transaction.TransactionType mapType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Transaction.TransactionType.valueOf(value.trim().toUpperCase());
    }
}