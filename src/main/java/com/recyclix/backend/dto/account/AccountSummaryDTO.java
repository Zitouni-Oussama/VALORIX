package com.recyclix.backend.dto.account;

import com.recyclix.backend.model.Account;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// dto/account/AccountSummaryDTO.java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountSummaryDTO {
    private Long id;
    private String email;
    private Account.RoleType roleType;
    private Account.AccountStatus status;
    private String profileImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String recyclingCenterName;

    // ===== NOUVEAUX CHAMPS =====
    private String firstName;
    private String lastName;
    private Integer totalPoints;          // pour client
    private Boolean isVerified;           // pour collector
    private BigDecimal averageRating;     // pour collector
    private String employeeNumber;        // pour factory user

}