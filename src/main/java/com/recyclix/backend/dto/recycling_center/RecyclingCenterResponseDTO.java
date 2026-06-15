package com.recyclix.backend.dto.recycling_center;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecyclingCenterResponseDTO {

    private Long id;

    private String name;
    private String address;

    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal capacity;
    private String contactEmail;
    private String contactPhone;

    private LocalDateTime createdAt;

    private Integer deliveries;
}