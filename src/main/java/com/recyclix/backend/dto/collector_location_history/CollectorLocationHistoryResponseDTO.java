package com.recyclix.backend.dto.collector_location_history;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectorLocationHistoryResponseDTO {

    private Long id;

    private Long collectorId;

    private BigDecimal latitude;
    private BigDecimal longitude;

    private LocalDateTime recordedAt;
}