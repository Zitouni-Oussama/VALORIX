package com.recyclix.backend.dto.truck;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TruckResponseDTO {

    private Long id;

    private String plateNumber;
    private String model;
    private double capacityKg;
    private String status;

    private Long collectorId;

    private LocalDateTime createdAt;

    private Integer collectionsCount;
    private String brand;
    private String truckPhotoUrl;
    private String greyCardImageUrl;
    private String drivingLicenseImageUrl;
}