package com.recyclix.backend.dto.support_ticket;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportTicketResponseDTO {

    private Long id;

    private Long accountId;
    private Long assignedToId;

    private String subject;
    private String description;

    private String status;
    private String priority;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}