package com.recyclix.backend.dto.challenge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketRequestChallengeResponse {
    private Long ticketId;
    private Long challengeId;
    private Long userChallengeId;
    private String status;
    private String message;
}