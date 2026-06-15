package com.recyclix.backend.dto.user_challenge;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserChallengeUpdateDTO {

    private String status;
    private Integer progress;
    private LocalDateTime completedAt;
}