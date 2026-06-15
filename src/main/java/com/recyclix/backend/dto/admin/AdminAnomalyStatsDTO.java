// recyclix\backend\dto\admin\AdminAnomalyStatsDTO.java
package com.recyclix.backend.dto.admin;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminAnomalyStatsDTO {

    private long totalAnomalies;
    private long criticalAnomalies;
    private long warningAnomalies;
    private long infoAnomalies;
}