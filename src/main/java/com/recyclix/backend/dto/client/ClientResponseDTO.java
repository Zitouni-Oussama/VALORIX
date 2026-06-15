package com.recyclix.backend.dto.client;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientResponseDTO {

    private Long id;

    private Long accountId;

    private String firstName;
    private String lastName;

    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;

    private Integer totalPoints;
    private LocalDateTime createdAt;

    // au lieu d'envoyer collectionRequests
    private Integer collectionRequestsCount;
}