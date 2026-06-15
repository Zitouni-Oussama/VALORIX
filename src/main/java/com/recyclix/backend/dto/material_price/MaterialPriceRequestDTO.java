package com.recyclix.backend.dto.material_price;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialPriceRequestDTO {

    @NotNull(message = "Le materialId ne peut pas être nul.")
    private Long materialId;

    @NotNull(message = "Le prix par kg ne peut pas être nul.")
    private BigDecimal pricePerKg;

    @NotNull(message = "La date d'effet ne peut pas être nulle.")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;
}