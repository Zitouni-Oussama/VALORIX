package com.recyclix.backend.dto.support_ticket;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportTicketSummaryDTO {

    private Long id;

    private String subject;
    private String status;
    private String priority;

    private LocalDateTime createdAt;
    private String roleType;
}