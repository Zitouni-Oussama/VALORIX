package com.recyclix.backend.dto.challenge;

import com.recyclix.backend.model.Challenge;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChallengeRequestDTO {

    @NotNull(message = "Le titre du défi ne peut pas être nul.")
    @Size(max = 100, message = "Le titre ne peut pas dépasser 100 caractères.")
    private String title;

    @Size(max = 500, message = "La description du défi ne peut pas dépasser 500 caractères.")
    private String description;

    @NotNull(message = "Le type de défi est obligatoire.")
    private Challenge.ChallengeType challengeType;

    private Long targetMaterialId;

    @NotNull(message = "La valeur cible ne peut pas être nulle.")
    private Integer targetValue;

    @NotNull(message = "L'unité est obligatoire.")
    private Challenge.Unit unit;

    @NotNull(message = "Les points de récompense ne peuvent pas être nuls.")
    private Integer rewardPoints;

    private Integer bonusPoints;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Boolean isActive;

    private Boolean autoValidate;

    private BigDecimal minQuantityPerCollection;

    private BigDecimal maxQuantityPerCollection;
}