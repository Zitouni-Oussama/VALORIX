// recyclix\backend\dto\admin\AdminSupportStatsDTO.java
package com.recyclix.backend.dto.admin;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminSupportStatsDTO {

    private long totalTickets;
    private long openTickets;
    private long inProgressTickets;
    private long closedTickets;
    private long unassignedTickets;

    private long totalFaqs;
    private long activeFaqs;

    private long citizenTickets;
    private long collectorTickets;
}