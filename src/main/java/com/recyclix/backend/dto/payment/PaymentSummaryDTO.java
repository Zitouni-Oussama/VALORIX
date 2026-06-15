package com.recyclix.backend.dto.payment;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSummaryDTO {

    private Long id;

    private BigDecimal amount;
    private String status;

    private LocalDateTime createdAt;

    private String description;
}