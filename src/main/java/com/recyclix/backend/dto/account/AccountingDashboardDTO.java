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
public class AccountingDashboardDTO {

    private BigDecimal totalCollectedRevenue;
    private BigDecimal totalExpectedRevenue;
    private int urgentOverdueCount;

    private Long totalInvoices;
    private Long paidInvoices;
    private Long pendingInvoices;
    private Long overdueInvoices;

    private Long totalCitizenPoints;
    private BigDecimal totalCitizenLiabilities;

    private BigDecimal totalExpenses;
    private BigDecimal estimatedProfit;
}