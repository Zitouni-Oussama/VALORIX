package com.recyclix.backend.service.accountant;

import com.recyclix.backend.dto.account.AccountingDashboardDTO;
import com.recyclix.backend.dto.account.MonthlyFinancialDTO;
import com.recyclix.backend.model.FactoryInvoice;
import com.recyclix.backend.repository.CollectionRepository;
import com.recyclix.backend.repository.FactoryInvoiceRepository;
import com.recyclix.backend.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;


@Service
@RequiredArgsConstructor
public class AccountingDashboardService {

    private final FactoryInvoiceRepository factoryInvoiceRepository;
    private final WalletRepository walletRepository;
    private final PaymentTrackingService paymentTrackingService;
    private final ExpenseManagementService expenseManagementService;
    private final CollectionRepository collectionRepository;

    private static final BigDecimal POINT_VALUE_IN_DA = new BigDecimal("0.5");

    @Transactional(readOnly = true)
    public AccountingDashboardDTO getDashboardMetrics() {

        // ✅ CORRIGÉ : utilisation de amountTtc pour les revenus collectés
        BigDecimal collectedRevenue = safe(
                factoryInvoiceRepository.sumAmountTtcByStatuses(
                        List.of(FactoryInvoice.InvoiceStatus.PAID)
                )
        );

        BigDecimal expectedRevenue = safe(paymentTrackingService.getTotalExpectedRevenue());

        int overdueCount = paymentTrackingService.getUrgentOverdueInvoices().size();

        Long totalInvoices = factoryInvoiceRepository.count();
        Long paidInvoices = factoryInvoiceRepository.countByStatus(FactoryInvoice.InvoiceStatus.PAID);
        Long pendingInvoices = factoryInvoiceRepository.countByStatus(FactoryInvoice.InvoiceStatus.PENDING);
        Long overdueInvoices = factoryInvoiceRepository.countByStatus(FactoryInvoice.InvoiceStatus.OVERDUE);

        Long totalPoints = walletRepository.sumClientPoints();
        if (totalPoints == null) totalPoints = 0L;

        BigDecimal citizenLiabilities = BigDecimal.valueOf(totalPoints).multiply(POINT_VALUE_IN_DA);

        BigDecimal totalExpenses = safe(
                expenseManagementService.getTotalExpenses(LocalDate.of(2000, 1, 1), LocalDate.now())
        );

        BigDecimal estimatedProfit = collectedRevenue.subtract(totalExpenses).subtract(citizenLiabilities);

        return AccountingDashboardDTO.builder()
                .totalCollectedRevenue(collectedRevenue)
                .totalExpectedRevenue(expectedRevenue)
                .urgentOverdueCount(overdueCount)
                .totalInvoices(totalInvoices)
                .paidInvoices(paidInvoices)
                .pendingInvoices(pendingInvoices)
                .overdueInvoices(overdueInvoices)
                .totalCitizenPoints(totalPoints)
                .totalCitizenLiabilities(citizenLiabilities)
                .totalExpenses(totalExpenses)
                .estimatedProfit(estimatedProfit)
                .build();
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }


    @Transactional(readOnly = true)
    public List<MonthlyFinancialDTO> getMonthlyFinancialHistory(int monthsBack) {
        LocalDate endDate = LocalDate.now().withDayOfMonth(1).minusDays(1);
        LocalDate startDate = endDate.minusMonths(monthsBack - 1).withDayOfMonth(1);

        List<MonthlyFinancialDTO> result = new ArrayList<>();
        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            YearMonth ym = YearMonth.from(current);
            int month = ym.getMonthValue();
            int year = ym.getYear();
            LocalDate monthStart = current;
            LocalDate monthEnd = current.withDayOfMonth(current.lengthOfMonth());

            // Revenus : factures PAYÉES émises dans le mois
            BigDecimal revenue = factoryInvoiceRepository.sumAmountTtcByStatusesAndIssueDateBetween(
                    List.of(FactoryInvoice.InvoiceStatus.PAID), monthStart, monthEnd);
            if (revenue == null) revenue = BigDecimal.ZERO;

            // Dépenses enregistrées dans le mois
            BigDecimal expenses = expenseManagementService.getTotalExpenses(monthStart, monthEnd);
            if (expenses == null) expenses = BigDecimal.ZERO;

            // Coût des points (collectes du mois)
            LocalDateTime startDateTime = monthStart.atStartOfDay();
            LocalDateTime endDateTime = monthEnd.atTime(LocalTime.MAX);
            BigDecimal pointsCost = collectionRepository.sumEstimatedPointCostsByCollectedAtBetween(startDateTime, endDateTime);
            if (pointsCost == null) pointsCost = BigDecimal.ZERO;

            BigDecimal profit = revenue.subtract(expenses).subtract(pointsCost);

            result.add(MonthlyFinancialDTO.builder()
                    .month(month)
                    .year(year)
                    .revenue(revenue)
                    .profit(profit)
                    .build());

            current = current.plusMonths(1);
        }
        return result;
    }

}