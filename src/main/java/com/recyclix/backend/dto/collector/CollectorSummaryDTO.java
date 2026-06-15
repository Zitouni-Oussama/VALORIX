package com.recyclix.backend.dto.collector;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectorSummaryDTO {

    private Long id;
    private Long accountId;

    private String firstName;
    private String lastName;

    private Boolean isVerified;
    private BigDecimal averageRating;

    private LocalDateTime createdAt;
}