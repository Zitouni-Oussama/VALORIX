package com.recyclix.backend.dto.collector_location_history;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectorLocationHistoryUpdateDTO {

    // ⚠️ collectorId non modifiable (OneToOne unique + Setter NONE)
    private BigDecimal latitude;
    private BigDecimal longitude;
}