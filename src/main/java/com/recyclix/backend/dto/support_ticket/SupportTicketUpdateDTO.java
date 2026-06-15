package com.recyclix.backend.dto.support_ticket;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportTicketUpdateDTO {

    private String status;
    private String priority;
    private Long assignedToId;
}