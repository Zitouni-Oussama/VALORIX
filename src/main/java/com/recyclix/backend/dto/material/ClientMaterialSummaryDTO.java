package com.recyclix.backend.dto.material;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientMaterialSummaryDTO {
    private Long id;
    private String name;
    private BigDecimal citizenPricePerKg;
    private BigDecimal collectorPricePerKg;
    private Boolean isActive;
}