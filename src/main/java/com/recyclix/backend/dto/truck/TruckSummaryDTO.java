package com.recyclix.backend.dto.truck;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TruckSummaryDTO {

    private Long id;
    private String plateNumber;
    private double capacityKg;
    private String status;
    private String truckPhotoUrl;
    private String greyCardImageUrl;
    private String drivingLicenseImageUrl;
    private String brand;
}