package com.recyclix.backend.mapper;

import com.recyclix.backend.dto.account.AccountRequestDTO;
import com.recyclix.backend.dto.account.AccountResponseDTO;
import com.recyclix.backend.dto.account.AccountSummaryDTO;
import com.recyclix.backend.dto.account.AccountUpdateDTO;
import com.recyclix.backend.model.Account;
import com.recyclix.backend.model.FactoryUser;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface AccountMapper {

    // =========================
    // Entity -> Response DTO
    // =========================
    @Mappings({
            @Mapping(target = "clientId", expression = "java(account.getClient() != null ? account.getClient().getId() : null)"),
            @Mapping(target = "collectorId", expression = "java(account.getCollector() != null ? account.getCollector().getId() : null)"),
            @Mapping(target = "factoryUserId", expression = "java(account.getFactoryUser() != null ? account.getFactoryUser().getId() : null)"),
            @Mapping(target = "walletId", expression = "java(account.getWallet() != null ? account.getWallet().getId() : null)"),
            @Mapping(target = "transactionsCount", expression = "java(account.getTransactions() != null ? account.getTransactions().size() : 0)"),
            @Mapping(target = "pointMovementsCount", expression = "java(account.getPointMovements() != null ? account.getPointMovements().size() : 0)"),
            @Mapping(target = "paymentsCount", expression = "java(account.getPayments() != null ? account.getPayments().size() : 0)"),
            @Mapping(target = "notificationsCount", expression = "java(account.getNotifications() != null ? account.getNotifications().size() : 0)"),
            @Mapping(target = "userChallengesCount", expression = "java(account.getUserChallenges() != null ? account.getUserChallenges().size() : 0)"),
            @Mapping(target = "supportTicketsCount", expression = "java(account.getSupportTickets() != null ? account.getSupportTickets().size() : 0)"),
            @Mapping(target = "leaderboardsCount", expression = "java(account.getClient() != null && account.getClient().getLeaderboards() != null ? account.getClient().getLeaderboards().size() : 0)")
    })
    AccountResponseDTO toDto(Account account);

    // =========================
    // Entity -> Summary DTO
    // =========================
    @Mapping(target = "firstName", expression = "java(getFirstName(account))")
    @Mapping(target = "lastName", expression = "java(getLastName(account))")
    @Mapping(target = "totalPoints", expression = "java(account.getClient() != null ? account.getClient().getTotalPoints() : null)")
    @Mapping(target = "isVerified", expression = "java(account.getCollector() != null ? account.getCollector().getIsVerified() : null)")
    @Mapping(target = "averageRating", expression = "java(account.getCollector() != null ? account.getCollector().getAverageRating() : null)")
    @Mapping(target = "employeeNumber", expression = "java(account.getFactoryUser() != null ? account.getFactoryUser().getEmployeeNumber() : null)")
    @Mapping(target = "recyclingCenterName", expression = "java(getRecyclingCenterName(account.getFactoryUser()))")
    AccountSummaryDTO toSummaryDto(Account account);

    // =========================
    // Request DTO -> Entity
    // =========================
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "client", ignore = true),
            @Mapping(target = "collector", ignore = true),
            @Mapping(target = "factoryUser", ignore = true),
            @Mapping(target = "wallet", ignore = true),
            @Mapping(target = "transactions", ignore = true),
            @Mapping(target = "pointMovements", ignore = true),
            @Mapping(target = "payments", ignore = true),
            @Mapping(target = "notifications", ignore = true),
            @Mapping(target = "userChallenges", ignore = true),
            @Mapping(target = "supportTickets", ignore = true)
    })
    Account toEntity(AccountRequestDTO dto);

    // =========================
    // Update DTO -> existing Entity
    // =========================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "email", ignore = true),
            @Mapping(target = "roleType", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "client", ignore = true),
            @Mapping(target = "collector", ignore = true),
            @Mapping(target = "factoryUser", ignore = true),
            @Mapping(target = "wallet", ignore = true),
            @Mapping(target = "transactions", ignore = true),
            @Mapping(target = "pointMovements", ignore = true),
            @Mapping(target = "payments", ignore = true),
            @Mapping(target = "notifications", ignore = true),
            @Mapping(target = "userChallenges", ignore = true),
            @Mapping(target = "supportTickets", ignore = true)
    })
    void updateEntityFromDto(AccountUpdateDTO dto, @MappingTarget Account entity);

    // =========================
    // Helpers
    // =========================
    default String getFirstName(Account account) {
        if (account.getClient() != null) return account.getClient().getFirstName();
        if (account.getCollector() != null) return account.getCollector().getFirstName();
        if (account.getFactoryUser() != null) return account.getFactoryUser().getFirstName();
        return null;
    }

    default String getLastName(Account account) {
        if (account.getClient() != null) return account.getClient().getLastName();
        if (account.getCollector() != null) return account.getCollector().getLastName();
        if (account.getFactoryUser() != null) return account.getFactoryUser().getLastName();
        return null;
    }

    default String getRecyclingCenterName(FactoryUser factoryUser) {
        return factoryUser != null && factoryUser.getRecyclingCenter() != null
                ? factoryUser.getRecyclingCenter().getName()
                : null;
    }
}