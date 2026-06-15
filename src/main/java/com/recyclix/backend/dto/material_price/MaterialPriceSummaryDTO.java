package com.recyclix.backend.dto.material_price;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialPriceSummaryDTO {

    private Long id;

    private Long materialId;

    private BigDecimal pricePerKg;

    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
}