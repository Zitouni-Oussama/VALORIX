// recyclix\backend\dto\admin\AdminAnomalyDTO.java
package com.recyclix.backend.dto.admin;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminAnomalyDTO {

    private String anomalyType;
    private String severity;
    private String description;
    private Long relatedEntityId;
    private String relatedEntityType;
    private LocalDateTime detectedAt;
}