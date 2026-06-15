package com.recyclix.backend.dto.collector;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectorResponseDTO {

    private Long id;
    private Long accountId;

    private String firstName;
    private String lastName;
    private String nationalIdNumber;

    private Boolean isVerified;
    private BigDecimal averageRating;

    private BigDecimal currentLatitude;
    private BigDecimal currentLongitude;

    private LocalDateTime createdAt;

    // relations (IDs)
    private Long truckId;
    private Long collectorLocationHistoryId;

    // collections (count)
    private Integer collectionsCount;

    private String accountStatus;
}