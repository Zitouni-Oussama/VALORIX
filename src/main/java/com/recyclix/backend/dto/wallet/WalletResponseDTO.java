package com.recyclix.backend.dto.wallet;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletResponseDTO {

    private Long id;

    private Long accountId;

    private BigDecimal balance;
    private String currency;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Integer transactions;
}