package com.recyclix.backend.dto.accountant;

import com.recyclix.backend.dto.account.AccountingDashboardDTO;
import com.recyclix.backend.dto.expense.ExpenseSummaryDTO;
import com.recyclix.backend.dto.factory_invoice.FactoryInvoiceResponseDTO;
import com.recyclix.backend.service.accountant.AccountantRewardService.RewardRedemptionDetailResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountantDashboardFullDTO {
    private AccountingDashboardDTO metrics;
    private List<RewardRedemptionDetailResponse> pendingRedemptions;
    private List<ExpenseSummaryDTO> recentExpenses;
    private List<FactoryInvoiceResponseDTO> recentInvoices;
    private Long totalRedemptionsPending;
    private Long totalExpensesCount;
    private Long totalInvoicesCount;
}