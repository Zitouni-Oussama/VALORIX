package com.recyclix.backend.dto.leaderboard;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardSummaryDTO {

    private Long id;
    private Long clientId;

    private String period;
    private Integer rank;
    private Integer points;

    private LocalDateTime createdAt;
}