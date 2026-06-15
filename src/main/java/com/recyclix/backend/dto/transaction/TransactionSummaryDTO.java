package com.recyclix.backend.dto.transaction;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionSummaryDTO {

    private Long id;

    private String type;
    private BigDecimal amount;

    private LocalDateTime createdAt;
}