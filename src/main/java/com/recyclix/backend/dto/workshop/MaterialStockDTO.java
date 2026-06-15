package com.recyclix.backend.dto.workshop;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class MaterialStockDTO {
    private Long materialId;
    private String materialName;
    private BigDecimal quantityKg;
    private LocalDateTime lastUpdated;
}