package com.recyclix.backend.dto.account;

import com.recyclix.backend.model.Account.AccountStatus;
import com.recyclix.backend.model.Account.RoleType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponseDTO {

    private Long id;

    private String email;
    private String phone;

    private RoleType roleType;
    private String profileImageUrl;
    private AccountStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ===== Relations 1-1 (IDs) =====
    private Long clientId;
    private Long collectorId;
    private Long factoryUserId;
    private Long walletId;

    // ===== Collections (counts) =====
    private Integer transactionsCount;
    private Integer pointMovementsCount;
    private Integer paymentsCount;
    private Integer notificationsCount;
    private Integer userChallengesCount;
    private Integer leaderboardsCount;
    private Integer supportTicketsCount;
}