package com.recyclix.backend.mapper;

import com.recyclix.backend.dto.wallet.WalletRequestDTO;
import com.recyclix.backend.dto.wallet.WalletResponseDTO;
import com.recyclix.backend.dto.wallet.WalletSummaryDTO;
import com.recyclix.backend.dto.wallet.WalletUpdateDTO;
import com.recyclix.backend.model.Wallet;
import org.mapstruct.*;

import java.math.BigDecimal;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface WalletMapper {

    // =========================
    // Entity -> Response DTO
    // =========================
    @Mappings({
            @Mapping(target = "accountId", expression = "java(entity.getAccount() != null ? entity.getAccount().getId() : null)"),

            // Convention artificielle : balance = balanceMoney
            @Mapping(target = "balance", source = "balanceMoney"),

            // Champs absents du modèle
            @Mapping(target = "currency", ignore = true),
            @Mapping(target = "transactions", ignore = true)
    })
    WalletResponseDTO toResponseDTO(Wallet entity);

    // =========================
    // Entity -> Summary DTO
    // =========================
    @Mappings({
            // Convention artificielle : balance = balanceMoney
            @Mapping(target = "balance", source = "balanceMoney"),
            @Mapping(target = "currency", ignore = true)
    })
    WalletSummaryDTO toSummaryDTO(Wallet entity);

    // =========================
    // Request DTO -> Entity
    // =========================
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "account", ignore = true), // injecté dans le service
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),

            // Initialisation par défaut
            @Mapping(target = "balanceMoney", expression = "java(java.math.BigDecimal.ZERO)"),
            @Mapping(target = "balancePoints", expression = "java(0)")
    })
    Wallet toEntity(WalletRequestDTO dto);

    // =========================
    // Update DTO -> Entity existante
    // =========================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "account", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "balanceMoney", ignore = true),
            @Mapping(target = "balancePoints", ignore = true)
    })
    void updateEntityFromDTO(WalletUpdateDTO dto, @MappingTarget Wallet entity);
}