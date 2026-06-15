package com.recyclix.backend.dto.leaderboard;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardRequestDTO {

    @NotNull(message = "Le clientId ne peut pas être nul.")
    private Long clientId;

    @NotNull(message = "La période ne peut pas être nulle.")
    @Size(max = 20, message = "La période ne peut pas dépasser 20 caractères.")
    private String period;

    @NotNull(message = "Le rang ne peut pas être nul.")
    private Integer rank;

    @NotNull(message = "Les points ne peuvent pas être nuls.")
    private Integer points;
}