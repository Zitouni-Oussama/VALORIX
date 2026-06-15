package com.recyclix.backend.dto.material;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientMaterialDetailDTO {
    private Long id;
    private String name;
    private String description;
    private Boolean isActive;

    private BigDecimal citizenPricePerKg;
    private BigDecimal collectorPricePerKg;

    private LocalDateTime priceStartDate;
    private LocalDateTime priceEndDate;
    private LocalDateTime createdAt;
}