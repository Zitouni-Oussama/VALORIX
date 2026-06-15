package com.recyclix.backend.dto.challenge;

import com.recyclix.backend.model.Challenge;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChallengeUpdateDTO {

    @Size(max = 100, message = "Le titre ne peut pas dépasser 100 caractères.")
    private String title;

    @Size(max = 500, message = "La description du défi ne peut pas dépasser 500 caractères.")
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
}