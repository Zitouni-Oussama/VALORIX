package com.recyclix.backend.dto.admin;

import com.recyclix.backend.model.Account;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminUserDetailsDTO {
    // Compte
    private Long id;
    private String email;
    private String phone;
    private Account.RoleType roleType;
    private Account.AccountStatus status;
    private String profileImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Informations personnelles (selon rôle)
    private String firstName;
    private String lastName;
    private String fullName;
    private String address;          // Client
    private BigDecimal latitude;      // Client
    private BigDecimal longitude;     // Client
    private Integer totalPoints;      // Client
    private String nationalIdNumber; // Collecteur
    private Boolean isVerified;      // Collecteur
    private BigDecimal averageRating;// Collecteur
    private String employeeNumber;   // FactoryUser
    private String position;         // FactoryUser
    private Long factoryUserId;

    // Statistiques
    private Integer transactionsCount;
    private Integer pointMovementsCount;
    private Integer paymentsCount;
    private Integer notificationsCount;
    private Integer userChallengesCount;
    private Integer leaderboardsCount;
    private Integer supportTicketsCount;

    private Boolean isHeadAccountant;
}