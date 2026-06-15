package com.recyclix.backend.dto.recycling_center;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecyclingCenterSummaryDTO {

    private Long id;
    private String name;
    private String address;
    private LocalDateTime createdAt;
    private String contactEmail;
    private String contactPhone;
    private BigDecimal capacity;
}