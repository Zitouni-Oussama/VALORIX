// recyclix\backend\dto\admin\AdminNotificationStatsDTO.java
package com.recyclix.backend.dto.admin;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminNotificationStatsDTO {

    private long totalNotificationsSent;
    private long totalClients;
    private long totalCollectors;
    private long totalFactoryUsers;
}