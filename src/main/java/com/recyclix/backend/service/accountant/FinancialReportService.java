package com.recyclix.backend.service.accountant;

import com.recyclix.backend.dto.financial_report.FinancialReportRequestDTO;
import com.recyclix.backend.dto.financial_report.FinancialReportResponseDTO;
import com.recyclix.backend.dto.financial_report.FinancialReportSummaryDTO;
import com.recyclix.backend.dto.financial_report.FinancialReportUpdateDTO;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.exception.UnauthorizedException;
import com.recyclix.backend.mapper.FinancialReportMapper;
import com.recyclix.backend.model.FactoryInvoice;
import com.recyclix.backend.model.FactoryUser;
import com.recyclix.backend.model.FinancialReport;
import com.recyclix.backend.repository.CollectionRepository;
import com.recyclix.backend.repository.FactoryInvoiceRepository;
import com.recyclix.backend.repository.FactoryUserRepository;
import com.recyclix.backend.repository.FinancialReportRepository;
import com.recyclix.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FinancialReportService {

    private final FinancialReportRepository financialReportRepository;
    private final FactoryInvoiceRepository factoryInvoiceRepository;
    private final ExpenseManagementService expenseManagementService;
    private final CollectionRepository collectionRepository;
    private final FactoryUserRepository factoryUserRepository;
    private final FinancialReportMapper financialReportMapper;

    // ========================= CREATE =========================

    public FinancialReportResponseDTO generateAndSaveReport(FinancialReportRequestDTO request) {
        validatePeriod(request.getPeriodStart(), request.getPeriodEnd());

        if (request.getReportType() == null) {
            throw new BadRequestException("Le type de rapport est obligatoire.");
        }

        financialReportRepository
                .findByReportTypeAndPeriodStartAndPeriodEnd(
                        request.getReportType(),
                        request.getPeriodStart(),
                        request.getPeriodEnd()
                )
                .ifPresent(r -> {
                    throw new BadRequestException("Un rapport existe déjà pour cette période.");
                });

        FactoryUser generatedBy = getAuthenticatedFactoryUser();

        FinancialReport report = buildCalculatedReport(
                request.getReportType(),
                request.getPeriodStart(),
                request.getPeriodEnd(),
                request.getFilePath(),
                generatedBy
        );

        return financialReportMapper.toResponseDTO(
                financialReportRepository.save(report)
        );
    }

    // ========================= PREVIEW =========================

    @Transactional(readOnly = true)
    public FinancialReportResponseDTO previewReport(LocalDate startDate, LocalDate endDate) {
        validatePeriod(startDate, endDate);

        FinancialReport report = buildCalculatedReport(
                FinancialReport.ReportType.CUSTOM,
                startDate,
                endDate,
                null,
                null
        );

        return financialReportMapper.toResponseDTO(report);
    }

    // ========================= READ =========================

    @Transactional(readOnly = true)
    public Page<FinancialReportSummaryDTO> getAllReports(Pageable pageable) {
        return financialReportRepository.findAll(pageable)
                .map(financialReportMapper::toSummaryDTO);
    }

    @Transactional(readOnly = true)
    public FinancialReportResponseDTO getReportById(Long id) {
        FinancialReport report = financialReportRepository.findWithGeneratorById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rapport introuvable avec ID : " + id));

        return financialReportMapper.toResponseDTO(report);
    }

    @Transactional(readOnly = true)
    public List<FinancialReportSummaryDTO> getReportsByType(FinancialReport.ReportType type) {
        return financialReportRepository.findAllByReportType(type)
                .stream()
                .map(financialReportMapper::toSummaryDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FinancialReportSummaryDTO> getReportsByPeriod(LocalDate start, LocalDate end) {
        validatePeriod(start, end);

        return financialReportRepository
                .findAllByPeriodStartGreaterThanEqualAndPeriodEndLessThanEqual(start, end)
                .stream()
                .map(financialReportMapper::toSummaryDTO)
                .toList();
    }

    // ========================= UPDATE =========================

    public FinancialReportResponseDTO updateReport(Long id, FinancialReportUpdateDTO request) {
        FinancialReport report = financialReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rapport introuvable avec ID : " + id));

        LocalDate start = request.getPeriodStart() != null ? request.getPeriodStart() : report.getPeriodStart();
        LocalDate end = request.getPeriodEnd() != null ? request.getPeriodEnd() : report.getPeriodEnd();

        validatePeriod(start, end);

        if (request.getReportType() != null) {
            report.setReportType(request.getReportType());
        }

        report.setPeriodStart(start);
        report.setPeriodEnd(end);

        if (request.getFilePath() != null) {
            report.setFilePath(request.getFilePath());
        }

        // 🔥 recalcul automatique
        FinancialReport recalculated = buildCalculatedReport(
                report.getReportType(),
                start,
                end,
                report.getFilePath(),
                report.getGeneratedBy()
        );

        report.setTotalIncome(recalculated.getTotalIncome());
        report.setTotalExpense(recalculated.getTotalExpense());
        report.setNetProfit(recalculated.getNetProfit());

        return financialReportMapper.toResponseDTO(
                financialReportRepository.save(report)
        );
    }

    // ========================= DELETE =========================

    public void deleteReport(Long id) {
        FinancialReport report = financialReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rapport introuvable avec ID : " + id));

        financialReportRepository.delete(report);
    }

    // ========================= CORE LOGIC =========================

    private FinancialReport buildCalculatedReport(
            FinancialReport.ReportType type,
            LocalDate start,
            LocalDate end,
            String filePath,
            FactoryUser generatedBy
    ) {
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);

        BigDecimal income = safe(
                factoryInvoiceRepository.sumAmountTtcByStatusesAndIssueDateBetween(
                        List.of(FactoryInvoice.InvoiceStatus.PAID), start, end
                )
        );

        BigDecimal expenses = safe(
                expenseManagementService.getTotalExpenses(start, end)
        );

        BigDecimal pointsCost = safe(
                collectionRepository.sumEstimatedPointCostsByCollectedAtBetween(
                        startDateTime,
                        endDateTime
                )
        );

        BigDecimal totalExpense = expenses.add(pointsCost);
        BigDecimal net = income.subtract(totalExpense);

        return FinancialReport.builder()
                .reportType(type)
                .periodStart(start)
                .periodEnd(end)
                .totalIncome(income)
                .totalExpense(totalExpense)
                .netProfit(net)
                .filePath(filePath)
                .generatedBy(generatedBy)
                .build();
    }

    // ========================= UTILS =========================

    private FactoryUser getAuthenticatedFactoryUser() {
        Long accountId = SecurityUtils.getAccountId()
                .orElseThrow(() -> new UnauthorizedException("Utilisateur non authentifié"));

        return factoryUserRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("FactoryUser introuvable"));
    }

    private void validatePeriod(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new BadRequestException("Les dates sont obligatoires.");
        }

        if (start.isAfter(end)) {
            throw new BadRequestException("La date de début doit être avant la date de fin.");
        }
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}