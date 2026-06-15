// recyclix\backend\dto\admin\DisputedRequestDTO.java
package com.recyclix.backend.dto.admin;

import com.recyclix.backend.model.CollectionRequest.Status;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisputedRequestDTO {

    private Long requestId;
    private Status requestStatus;

    private Long clientId;
    private String clientFullName;

    private Long collectorId;
    private String collectorFullName;

    private BigDecimal estimatedAmount;
    private String disputeReason;

    private Long relatedTicketId;
    private String ticketSubject;
    private String ticketStatus;

    private LocalDateTime createdAt;
}