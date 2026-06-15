// recyclix\backend\dto\admin\SupervisionStatsDTO.java
package com.recyclix.backend.dto.admin;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupervisionStatsDTO {

    private long totalRequests;
    private long pendingRequests;
    private long acceptedRequests;
    private long collectedRequests;
    private long deliveredRequests;
    private long cancelledRequests;
    private long disputedRequests;
    private long collectionsPendingValidation;

    private long totalCollections;
    private long totalDeliveries;
    private long validatedDeliveries;
    private long refusedDeliveries;
}