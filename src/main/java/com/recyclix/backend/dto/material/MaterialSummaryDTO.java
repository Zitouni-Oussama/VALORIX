package com.recyclix.backend.dto.material;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialSummaryDTO {

    private Long id;
    private String name;
    private BigDecimal basePricePerKg;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private BigDecimal collectorPricePerKg;
}