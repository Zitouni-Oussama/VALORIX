package com.recyclix.backend.dto.client;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientSummaryDTO {

    private Long id;
    private Long accountId;

    private String firstName;
    private String lastName;

    private Integer totalPoints;
    private LocalDateTime createdAt;
}