package com.recyclix.backend.dto.collector_location_history;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectorLocationHistorySummaryDTO {

    private Long id;
    private Long collectorId;
    private LocalDateTime recordedAt;
}