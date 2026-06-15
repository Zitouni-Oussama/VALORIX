package com.recyclix.backend.dto.leaderboard;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardUpdateDTO {

    // ⚠️ clientId et period généralement non modifiables
    private Integer rank;
    private Integer points;
}