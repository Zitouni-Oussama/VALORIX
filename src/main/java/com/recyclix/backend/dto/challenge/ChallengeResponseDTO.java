package com.recyclix.backend.dto.challenge;

import com.recyclix.backend.model.Challenge;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChallengeResponseDTO {

    private Long id;
    private String title;
    private String description;
    private Challenge.ChallengeType challengeType;
    private Long targetMaterialId;
    private Integer targetValue;
    private Challenge.Unit unit;
    private Integer rewardPoints;
    private Integer bonusPoints;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private Boolean autoValidate;
    private BigDecimal minQuantityPerCollection;
    private BigDecimal maxQuantityPerCollection;
    private LocalDateTime createdAt;
    private Integer userChallengesCount;
}