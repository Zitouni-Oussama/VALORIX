package com.recyclix.backend.dto.truck;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TruckUpdateDTO {

    @Size(max = 20)
    private String plateNumber;

    @Size(max = 100)
    private String model;

    private BigDecimal capacityKg;
    private String status;
    private String brand;
    private Long collectorId;
    private String truckPhotoUrl;
    private String greyCardImageUrl;
    private String drivingLicenseImageUrl;
}