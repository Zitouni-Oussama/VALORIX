package com.recyclix.backend.dto.wallet;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletSummaryDTO {

    private Long id;

    private BigDecimal balance;
    private String currency;
}