package com.recyclix.backend.dto.workshop;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TreatmentBatchDTO {
    private Long id;
    private String batchNumber;
    private Long materialId;
    private String materialName;
    private BigDecimal initialQuantityKg;
    private BigDecimal processedQuantityKg;
    private String status; // PROCESSING, COMPLETED
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String startedBy; // nom du factory user
    private String notes;
}