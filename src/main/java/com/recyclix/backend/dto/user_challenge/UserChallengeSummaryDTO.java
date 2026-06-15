package com.recyclix.backend.dto.user_challenge;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserChallengeSummaryDTO {

    private Long id;

    private Long accountId;
    private Long challengeId;

    private String status;
    private Integer progress;

    private LocalDateTime completedAt;
    private LocalDateTime createdAt;

    private String challengeTitle;
    private String challengeDescription;
    private Double targetValue;

}