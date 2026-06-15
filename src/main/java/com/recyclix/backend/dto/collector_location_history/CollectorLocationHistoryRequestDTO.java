package com.recyclix.backend.dto.collector_location_history;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectorLocationHistoryRequestDTO {

    @NotNull(message = "Le collectorId ne peut pas être nul.")
    private Long collectorId;

    @NotNull(message = "La latitude ne peut pas être nulle.")
    private BigDecimal latitude;

    @NotNull(message = "La longitude ne peut pas être nulle.")
    private BigDecimal longitude;
}