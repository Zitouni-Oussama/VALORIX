package com.recyclix.backend.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyFinancialDTO {
    private int month;      // 1 à 12
    private int year;
    private BigDecimal revenue;
    private BigDecimal profit;
}