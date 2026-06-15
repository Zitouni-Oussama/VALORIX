package com.recyclix.backend.dto.material;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialResponseDTO {

    private Long id;

    private String name;
    private String description;
    private BigDecimal basePricePerKg;

    private Boolean isActive;
    private LocalDateTime createdAt;

    private Integer collectionRequestsCount;
    private Integer aiClassificationsCount;
}