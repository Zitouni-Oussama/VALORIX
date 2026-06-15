package com.recyclix.backend.dto.material_price;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialPriceUpdateDTO {

    // ⚠️ materialId et effectiveFrom généralement non modifiables
    private BigDecimal pricePerKg;

    private LocalDate effectiveTo;
}