package com.recyclix.backend.controller.web;

import com.recyclix.backend.controller.accountant.AccountantRewardController;
import com.recyclix.backend.controller.accountant.ExpenseController;
import com.recyclix.backend.dto.account.AccountingDashboardDTO;
import com.recyclix.backend.dto.account.MonthlyFinancialDTO;
import com.recyclix.backend.dto.collector.CollectorSummaryDTO;
import com.recyclix.backend.dto.collector_payment.PayValidationsRequest;
import com.recyclix.backend.dto.collector_payment.UnpaidValidationDTO;
import com.recyclix.backend.dto.employee.EmployeeRequestDTO;
import com.recyclix.backend.dto.employee.EmployeeResponseDTO;
import com.recyclix.backend.dto.employee.EmployeeSummaryDTO;
import com.recyclix.backend.dto.employee.EmployeeUpdateDTO;
import com.recyclix.backend.dto.factory_invoice.FactoryInvoiceRequestDTO;
import com.recyclix.backend.dto.factory_invoice.FactoryInvoiceResponseDTO;
import com.recyclix.backend.dto.financial_report.FinancialReportSummaryDTO;
import com.recyclix.backend.dto.payroll.DeductionRequestDTO;
import com.recyclix.backend.dto.payroll.DeductionResponseDTO;
import com.recyclix.backend.dto.payroll.DeductionSummaryDTO;
import com.recyclix.backend.dto.payroll.DeductionUpdateDTO;
import com.recyclix.backend.dto.transaction.TransactionResponseDTO;
import com.recyclix.backend.dto.worker_absence.WorkerAbsenceRequestDTO;
import com.recyclix.backend.dto.worker_absence.WorkerAbsenceResponseDTO;
import com.recyclix.backend.dto.worker_absence.WorkerAbsenceSummaryDTO;
import com.recyclix.backend.dto.worker_absence.WorkerAbsenceUpdateDTO;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.UnauthorizedException;
import com.recyclix.backend.mapper.CollectorMapper;
import com.recyclix.backend.mapper.TransactionMapper;
import com.recyclix.backend.model.*;
import com.recyclix.backend.repository.*;
import com.recyclix.backend.service.accountant.*;
import com.recyclix.backend.service.hr.EmployeeService;
import com.recyclix.backend.service.hr.PayrollDeductionService;
import com.recyclix.backend.service.hr.WorkerAbsenceService;
import com.recyclix.backend.util.ApiResponse;
import com.recyclix.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.recyclix.backend.dto.payment.PaymentResponseDTO;
import com.recyclix.backend.mapper.PaymentMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/accountant")
@PreAuthorize("@factoryAccess.hasPosition('ACCOUNTANT')")
@RequiredArgsConstructor
public class AccountantWebController {

    private final AccountingDashboardService dashboardService;
    private final FactoryInvoiceService factoryInvoiceService;
    private final FactoryInvoicePdfService pdfService;
    private final ExpenseManagementService expenseManagementService;
    private final AccountantRewardService accountantRewardService;
    private final RewardRedemptionRepository rewardRedemptionRepository;
    private final FinancialReportPdfService financialReportPdfService;
    private final FinancialReportService financialReportService;
    private final PaymentTrackingService paymentTrackingService;
    private final TreasuryAndWithdrawalService treasuryService;
    private final PaymentMapper paymentMapper;
    private final PaymentRepository paymentRepository;
    private final EmployeeService employeeService;
    private final WorkerAbsenceService absenceService;
    private final PayrollDeductionService deductionService;

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final WithdrawalApprovalService withdrawalApprovalService;

    // Pages HTML
    @GetMapping("/dashboard")
    public String dashboard() {
        return "accountant/dashboard";
    }

    @GetMapping("/invoices")
    public String invoices() {
        return "accountant/invoices";
    }

    @GetMapping("/expenses")
    public String expenses() {
        return "accountant/expenses";
    }

    // ===== PAGES RESTREINTES AUX CHEFS COMPTABLES =====
    @GetMapping("/rewards")
    public String rewards() {
        if (!isCurrentUserHeadAccountant()) {
            return "redirect:/accountant/dashboard?error=unauthorized";
        }
        return "accountant/rewards";
    }

    @GetMapping("/reports")
    public String reports() {
        if (!isCurrentUserHeadAccountant()) {
            return "redirect:/accountant/dashboard?error=unauthorized";
        }
        return "accountant/reports";
    }

    @GetMapping("/treasury")
    public String treasury() {
        if (!isCurrentUserHeadAccountant()) {
            return "redirect:/accountant/dashboard?error=unauthorized";
        }
        return "accountant/treasury";
    }

    @GetMapping("/employees")
    public String employees() {
        return "accountant/employees";
    }

    // Endpoints JSON pour le dashboard
    @GetMapping("/dashboard-data")
    @ResponseBody
    public AccountingDashboardDTO getDashboardData() {
        return dashboardService.getDashboardMetrics();
    }

    @GetMapping("/financial-history")
    @ResponseBody
    public ApiResponse<List<MonthlyFinancialDTO>> getFinancialHistory(@RequestParam(defaultValue = "6") int months) {
        return ApiResponse.ok(dashboardService.getMonthlyFinancialHistory(months));
    }

    // Endpoints JSON pour les factures (utilisés par le frontend)
    @GetMapping("/invoices-data")
    @ResponseBody
    public ApiResponse<Page<FactoryInvoiceResponseDTO>> getInvoicesData(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String factoryName,
            @RequestParam(required = false) BigDecimal minAmountHt,
            @RequestParam(required = false) BigDecimal maxAmountHt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startIssueDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endIssueDate) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("issueDate").descending());
        Page<FactoryInvoiceResponseDTO> result = factoryInvoiceService.getFilteredInvoices(
                status, factoryName, minAmountHt, maxAmountHt, startIssueDate, endIssueDate, pageable);
        return ApiResponse.ok(result);
    }

    @GetMapping("/invoices-stats")
    @ResponseBody
    public ApiResponse<FactoryInvoiceService.InvoiceStatistics> getInvoicesStats() {
        return ApiResponse.ok(factoryInvoiceService.getStatistics());
    }

    @GetMapping("/invoice/{id}")
    @ResponseBody
    public ApiResponse<FactoryInvoiceResponseDTO> getInvoice(@PathVariable Long id) {
        return ApiResponse.ok(factoryInvoiceService.getInvoiceById(id));
    }

    @PostMapping("/invoices")
    @ResponseBody
    public ApiResponse<FactoryInvoiceResponseDTO> createInvoice(@RequestBody FactoryInvoiceRequestDTO request) {
        return ApiResponse.ok("Facture créée", factoryInvoiceService.createInvoice(request));
    }

    @PutMapping("/invoices/{id}")
    @ResponseBody
    public ApiResponse<FactoryInvoiceResponseDTO> updateInvoice(@PathVariable Long id, @RequestBody FactoryInvoiceRequestDTO request) {
        return ApiResponse.ok("Facture mise à jour", factoryInvoiceService.updateInvoice(id, request));
    }

    @DeleteMapping("/invoices/{id}")
    @ResponseBody
    public ApiResponse<Void> deleteInvoice(@PathVariable Long id) {
        factoryInvoiceService.deleteInvoice(id);
        return ApiResponse.okMessage("Facture supprimée");
    }

    @PutMapping("/invoices/{id}/pay")
    @ResponseBody
    public ApiResponse<FactoryInvoiceResponseDTO> markAsPaid(@PathVariable Long id) {
        return ApiResponse.ok("Facture payée", factoryInvoiceService.markAsPaid(id));
    }

    @PutMapping("/invoices/{id}/overdue")
    @ResponseBody
    public ApiResponse<FactoryInvoiceResponseDTO> markAsOverdue(@PathVariable Long id) {
        return ApiResponse.ok("Facture en retard", factoryInvoiceService.markAsOverdue(id));
    }

    @PutMapping("/invoices/{id}/cancel")
    @ResponseBody
    public ApiResponse<FactoryInvoiceResponseDTO> cancelInvoice(@PathVariable Long id) {
        return ApiResponse.ok("Facture annulée", factoryInvoiceService.cancelInvoice(id));
    }

    @GetMapping("/invoices/{id}/pdf")
    @ResponseBody
    public ResponseEntity<Resource> downloadInvoicePdf(@PathVariable Long id) {
        return pdfService.generateFactoryInvoicePdf(id);
    }

    //* ----------------------- EXPENSE
    // Dans AccountantWebController.java

    @GetMapping("/expenses-data")
    @ResponseBody
    public ApiResponse<List<Expense>> getExpensesData(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<Expense> expenses = expenseManagementService.getAllExpenses();

        // Filtrage simple côté serveur (vous pouvez l'étendre)
        if (category != null && !category.isBlank()) {
            expenses = expenses.stream()
                    .filter(e -> e.getCategory().name().equals(category))
                    .collect(Collectors.toList());
        }
        if (startDate != null) {
            expenses = expenses.stream()
                    .filter(e -> !e.getExpenseDate().isBefore(startDate))
                    .collect(Collectors.toList());
        }
        if (endDate != null) {
            expenses = expenses.stream()
                    .filter(e -> !e.getExpenseDate().isAfter(endDate))
                    .collect(Collectors.toList());
        }

        return ApiResponse.ok(expenses);
    }

    @GetMapping("/expenses-stats")
    @ResponseBody
    public ApiResponse<Map<String, Object>> getExpensesStats() {
        List<Expense> expenses = expenseManagementService.getAllExpenses();
        double total = expenses.stream().mapToDouble(e -> e.getAmount().doubleValue()).sum();
        Map<String, Object> stats = Map.of("total", total, "count", expenses.size());
        return ApiResponse.ok(stats);
    }

    @PostMapping("/expenses-create")
    @ResponseBody
    public ApiResponse<Expense> createExpense(@RequestBody ExpenseController.ExpenseRequest request) {
        Expense newExpense = expenseManagementService.recordExpense(
                request.getDescription(),
                request.getAmount(),
                request.getCategory(),
                request.getExpenseDate()
        );
        return ApiResponse.ok("Dépense créée", newExpense);
    }

    @PutMapping("/expenses-update/{id}")
    @ResponseBody
    public ApiResponse<Expense> updateExpense(@PathVariable Long id, @RequestBody ExpenseController.ExpenseRequest request) {
        // Vous devez implémenter updateExpense dans ExpenseManagementService
        Expense updated = expenseManagementService.updateExpense(id, request);
        return ApiResponse.ok("Dépense mise à jour", updated);
    }

    @DeleteMapping("/expenses-delete/{id}")
    @ResponseBody
    public ApiResponse<Void> deleteExpense(@PathVariable Long id) {
        expenseManagementService.deleteExpense(id);
        return ApiResponse.okMessage("Dépense supprimée");
    }

    // ==================== RÉCOMPENSES (web endpoints) ====================

    @GetMapping("/rewards-data")
    @ResponseBody
    public ApiResponse<List<AccountantRewardService.RewardResponse>> getRewardsData() {
        return ApiResponse.ok(accountantRewardService.getAllRewards());
    }

    @GetMapping("/rewards-pending")
    @ResponseBody
    public ApiResponse<List<AccountantRewardService.RewardRedemptionDetailResponse>> getPendingRedemptionsWeb() {
        return ApiResponse.ok(accountantRewardService.getPendingRedemptions());
    }

    @PostMapping("/rewards-create")
    @ResponseBody
    public ApiResponse<AccountantRewardService.RewardResponse> createRewardWeb(@RequestBody AccountantRewardController.CreateRewardRequest request) {
        return ApiResponse.ok(accountantRewardService.createReward(request));
    }

    @PutMapping("/rewards-update/{id}")
    @ResponseBody
    public ApiResponse<AccountantRewardService.RewardResponse> updateRewardWeb(@PathVariable Long id, @RequestBody AccountantRewardController.UpdateRewardRequest request) {
        return ApiResponse.ok(accountantRewardService.updateReward(id, request));
    }

    @PutMapping("/rewards-toggle/{id}")
    @ResponseBody
    public ApiResponse<AccountantRewardService.RewardResponse> toggleRewardWeb(@PathVariable Long id) {
        return ApiResponse.ok(accountantRewardService.toggleReward(id));
    }

    @DeleteMapping("/rewards-delete/{id}")
    @ResponseBody
    public ApiResponse<Void> deleteRewardWeb(@PathVariable Long id) {
        accountantRewardService.deleteReward(id);
        return ApiResponse.okMessage("Récompense supprimée");
    }

    @PutMapping("/rewards-approve/{id}")
    @ResponseBody
    public ApiResponse<AccountantRewardService.RewardRedemptionDetailResponse> approveRedemptionWeb(@PathVariable Long id) {
        return ApiResponse.ok(accountantRewardService.approveRedemption(id, null));
    }

    @PutMapping("/rewards-reject/{id}")
    @ResponseBody
    public ApiResponse<AccountantRewardService.RewardRedemptionDetailResponse> rejectRedemptionWeb(@PathVariable Long id, @RequestBody Map<String, String> body) {
        AccountantRewardController.ReviewRequest req = new AccountantRewardController.ReviewRequest();
        req.setNote(body.get("note"));
        return ApiResponse.ok(accountantRewardService.rejectRedemption(id, req));
    }

//    @PutMapping("/rewards-deliver/{id}")
//    @ResponseBody
//    public ApiResponse<AccountantRewardService.RewardRedemptionDetailResponse> markDeliveredWeb(@PathVariable Long id) {
//        return ApiResponse.ok(accountantRewardService.markAsDelivered(id));
//    }

    @GetMapping("/rewards-history")
    @ResponseBody
    @Transactional(readOnly = true)
    public ApiResponse<Page<AccountantRewardService.RewardRedemptionDetailResponse>> getRedemptionHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<RewardRedemption> allRedemptions = rewardRedemptionRepository.findAll(pageable);
        Page<AccountantRewardService.RewardRedemptionDetailResponse> responsePage = allRedemptions.map(redemption -> {
            // Les associations sont initialisées dans la transaction
            Account account = redemption.getAccount();
            Reward reward = redemption.getReward();
            return AccountantRewardService.RewardRedemptionDetailResponse.builder()
                    .id(redemption.getId())
                    .accountEmail(account != null ? account.getEmail() : null)
                    .rewardName(reward != null ? reward.getName() : null)
                    .rewardCategory(reward != null && reward.getCategory() != null ? reward.getCategory().name() : null)
                    .pointsSpent(redemption.getPointsSpent())
                    .status(redemption.getStatus() != null ? redemption.getStatus().name() : null)
                    .redemptionCode(redemption.getRedemptionCode())
                    .additionalInfo(redemption.getAdditionalInfo())
                    .reviewNote(redemption.getReviewNote())
                    .expiryDate(redemption.getExpiryDate())
                    .createdAt(redemption.getCreatedAt())
                    .updatedAt(redemption.getUpdatedAt())
                    .build();
        });
        return ApiResponse.ok(responsePage);
    }

    @PutMapping("/{redemptionId}/deliver")
    public ResponseEntity<ApiResponse<?>> markAsDelivered(
            @PathVariable Long redemptionId,
            @RequestBody(required = false) AccountantRewardController.ReviewRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Marqué comme livré avec succès.",
                        accountantRewardService.markAsDelivered(redemptionId, request)
                )
        );
    }

    // ==================== RAPPORTS FINANCIERS (WEB) ====================

    @GetMapping("/reports-data")
    @ResponseBody
    public ApiResponse<Page<FinancialReportSummaryDTO>> getReportsData(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("generatedAt").descending());
        Page<FinancialReportSummaryDTO> reports = financialReportService.getAllReports(pageable);
        return ApiResponse.ok(reports);
    }

    @GetMapping("/reports-preview/pdf")
    @ResponseBody
    public ResponseEntity<Resource> previewReportPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return financialReportPdfService.generatePreviewPdf(startDate, endDate);
    }

    @PostMapping("/reports-generate/pdf")
    @ResponseBody
    public ResponseEntity<Resource> generateAndSaveReportPdf(
            @RequestParam FinancialReport.ReportType reportType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return financialReportPdfService.generateAndDownloadReport(reportType, startDate, endDate);
    }

    @GetMapping("/reports-download/{id}/pdf")
    @ResponseBody
    public ResponseEntity<Resource> downloadReportPdf(@PathVariable Long id) {
        return financialReportPdfService.generatePdfFromExistingReport(id);
    }

    // ==================== TRÉSORERIE & RETRAITS (WEB) ====================

    @GetMapping("/treasury-summary")
    @ResponseBody
    public ApiResponse<TreasuryAndWithdrawalService.TreasurySummary> getTreasurySummary() {
        return ApiResponse.ok(treasuryService.getTreasurySummary());
    }

    @GetMapping("/expected-revenue")
    @ResponseBody
    public ResponseEntity<BigDecimal> getExpectedRevenue() {
        return ResponseEntity.ok(paymentTrackingService.getTotalExpectedRevenue());
    }

    @GetMapping("/urgent-overdue")
    @ResponseBody
    public ResponseEntity<List<FactoryInvoice>> getUrgentOverdue() {
        return ResponseEntity.ok(paymentTrackingService.getUrgentOverdueInvoices());
    }

    @GetMapping("/treasury-pending")
    @ResponseBody
    @Transactional(readOnly = true)
    public ApiResponse<List<PaymentResponseDTO>> getPendingWithdrawals() {
        List<Payment> payments = treasuryService.getPendingWithdrawals();
        List<PaymentResponseDTO> dtos = payments.stream()
                .map(paymentMapper::toResponseDTO)
                .toList();
        return ApiResponse.ok(dtos);
    }

    @GetMapping("/treasury-history")
    @ResponseBody
    @Transactional(readOnly = true)
    public ApiResponse<Page<PaymentResponseDTO>> getTreasuryHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("paymentDate").descending());
        Page<Payment> paymentsPage = paymentRepository.findAllWithAccount(pageable);
        Page<PaymentResponseDTO> dtosPage = paymentsPage.map(paymentMapper::toResponseDTO);
        return ApiResponse.ok(dtosPage);
    }

    @PostMapping("/treasury-approve/{id}")
    @ResponseBody
    public ResponseEntity<String> approveWithdrawal(@PathVariable Long id) {
        treasuryService.approveWithdrawal(id);
        return ResponseEntity.ok("Retrait approuvé avec succès.");
    }

    @PostMapping("/treasury-reject/{id}")
    @ResponseBody
    public ResponseEntity<String> rejectWithdrawal(@PathVariable Long id, @RequestBody String reason) {
        treasuryService.rejectWithdrawal(id, reason);
        return ResponseEntity.ok("Retrait rejeté.");
    }

    @PostMapping("/trigger-check")
    @ResponseBody
    public ResponseEntity<String> triggerOverdueCheck() {
        paymentTrackingService.autoCheckOverdueInvoices();
        return ResponseEntity.ok("Vérification des retards effectuée.");
    }

    // Dans AccountantWebController.java

    // ==================== EMPLOYÉS ====================
    @GetMapping("/employees-data")
    @ResponseBody
    public ApiResponse<Page<EmployeeSummaryDTO>> getEmployeesData(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<EmployeeSummaryDTO> employees = employeeService.getAllEmployees(pageable);
        return ApiResponse.ok(employees);
    }

    @GetMapping("/employees-list")
    @ResponseBody
    public ApiResponse<List<EmployeeSummaryDTO>> getEmployeesList() {
        return ApiResponse.ok(employeeService.getAllEmployees());
    }

    @PostMapping("/employees-create")
    @ResponseBody
    public ApiResponse<EmployeeResponseDTO> createEmployee(@RequestBody EmployeeRequestDTO request) {
        return ApiResponse.ok(employeeService.createEmployee(request));
    }

    @PutMapping("/employees-update/{id}")
    @ResponseBody
    public ApiResponse<EmployeeResponseDTO> updateEmployee(@PathVariable Long id, @RequestBody EmployeeUpdateDTO request) {
        return ApiResponse.ok(employeeService.updateEmployee(id, request));
    }

    @PutMapping("/employees-activate/{id}")
    @ResponseBody
    public ApiResponse<EmployeeResponseDTO> activateEmployee(@PathVariable Long id) {
        return ApiResponse.ok(employeeService.activateEmployee(id));
    }

    @PutMapping("/employees-deactivate/{id}")
    @ResponseBody
    public ApiResponse<EmployeeResponseDTO> deactivateEmployee(@PathVariable Long id) {
        return ApiResponse.ok(employeeService.deactivateEmployee(id));
    }

    @DeleteMapping("/employees-delete/{id}")
    @ResponseBody
    public ApiResponse<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ApiResponse.okMessage("Employé supprimé");
    }

    // ==================== ABSENCES ====================
    @GetMapping("/absences-data")
    @ResponseBody
    public ApiResponse<List<WorkerAbsenceSummaryDTO>> getAbsencesData() {
        return ApiResponse.ok(absenceService.getAllAbsences());
    }

    @PostMapping("/absences-create")
    @ResponseBody
    public ApiResponse<WorkerAbsenceResponseDTO> createAbsence(@RequestBody WorkerAbsenceRequestDTO request) {
        return ApiResponse.ok(absenceService.reportAbsence(request));
    }

    @PutMapping("/absences-update/{id}")
    @ResponseBody
    public ApiResponse<WorkerAbsenceResponseDTO> updateAbsence(@PathVariable Long id, @RequestBody WorkerAbsenceUpdateDTO request) {
        return ApiResponse.ok(absenceService.updateAbsence(id, request));
    }

    @DeleteMapping("/absences-delete/{id}")
    @ResponseBody
    public ApiResponse<Void> deleteAbsence(@PathVariable Long id) {
        absenceService.deleteAbsence(id);
        return ApiResponse.okMessage("Absence supprimée");
    }

    // ==================== DÉDUCTIONS ====================
    @GetMapping("/deductions-data")
    @ResponseBody
    public ApiResponse<List<DeductionSummaryDTO>> getDeductionsData() {
        return ApiResponse.ok(deductionService.getAllDeductions());
    }

    @PostMapping("/deductions-create")
    @ResponseBody
    public ApiResponse<DeductionResponseDTO> createDeduction(@RequestBody DeductionRequestDTO request) {
        return ApiResponse.ok(deductionService.addDeduction(request));
    }

    @PutMapping("/deductions-update/{id}")
    @ResponseBody
    public ApiResponse<DeductionResponseDTO> updateDeduction(@PathVariable Long id, @RequestBody DeductionUpdateDTO request) {
        return ApiResponse.ok(deductionService.updateDeduction(id, request));
    }

    @DeleteMapping("/deductions-delete/{id}")
    @ResponseBody
    public ApiResponse<Void> deleteDeduction(@PathVariable Long id) {
        deductionService.deleteDeduction(id);
        return ApiResponse.okMessage("Retenue supprimée");
    }


    // Dans AccountantWebController.java
    private final FactoryUserRepository factoryUserRepository;

    private boolean isCurrentUserHeadAccountant() {
        Long accountId = SecurityUtils.getAccountId().orElse(null);
        if (accountId == null) return false;
        return factoryUserRepository.findByAccountId(accountId)
                .map(FactoryUser::getIsHeadAccountant)
                .orElse(false);
    }

    //**
    @GetMapping("/collector-payment")
    public String collectorPaymentPage() {
        return "accountant/collector-payment";
    }


    // ==================== PAIEMENT DES COLLECTEURS (AJOUT) ====================

    private final CollectorRepository collectorRepository;
    private final CollectorMapper collectorMapper;
    private final CollectorPaymentService collectorPaymentService;

    /**
     * Recherche de collecteurs pour auto-complétion
     */
    @GetMapping("/collectors/search")
    @ResponseBody
    public ApiResponse<List<CollectorSummaryDTO>> searchCollectors(@RequestParam String q) {
        if (q == null || q.isBlank()) {
            return ApiResponse.ok(List.of());
        }
        List<Collector> collectors = collectorRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrAccount_EmailContainingIgnoreCase(q, q, q);
        List<CollectorSummaryDTO> dtos = collectors.stream()
                .limit(10)
                .map(collectorMapper::toSummaryDto)
                .toList();
        return ApiResponse.ok(dtos);
    }

    /**
     * Récupère les validations non payées d'un collecteur
     */
    @GetMapping("/collector-payments/unpaid/{collectorId}")
    @ResponseBody
    public ApiResponse<List<UnpaidValidationDTO>> getUnpaidValidations(@PathVariable Long collectorId) {
        List<UnpaidValidationDTO> unpaid = collectorPaymentService.getUnpaidValidations(collectorId);
        return ApiResponse.ok(unpaid);
    }

    /**
     * Effectue le paiement des validations sélectionnées et retourne le PDF
     */
    @PostMapping("/collector-payments/pay/{collectorId}")
    public ResponseEntity<byte[]> payValidations(
            @PathVariable Long collectorId,
            @RequestBody PayValidationsRequest request) {
        byte[] pdfBytes = collectorPaymentService.payValidations(collectorId, request.getValidationIds(), request.getPaymentMethod());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"paiement_collecteur_" + collectorId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }



    // ==================== GESTION DES DEMANDES DE RETRAIT (TRANSACTIONS) ====================

    @GetMapping("/withdrawals/pending")
    @ResponseBody
    @Transactional(readOnly = true)
    public ApiResponse<List<TransactionResponseDTO>> getPendingWithdrawalTransactions() {
        List<Transaction> pending = transactionRepository.findByTypeAndStatus(
                Transaction.TransactionType.CITIZEN_PAYMENT,
                Transaction.TransactionStatus.PENDING
        );
        List<TransactionResponseDTO> dtos = pending.stream()
                .map(transactionMapper::toResponseDTO)
                .toList();
        return ApiResponse.ok(dtos);
    }

    @GetMapping("/withdrawals/history")
    @ResponseBody
    @Transactional(readOnly = true)
    public ApiResponse<Page<TransactionResponseDTO>> getWithdrawalHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Transaction> transactions = transactionRepository.findByType(
                Transaction.TransactionType.CITIZEN_PAYMENT,
                pageable
        );
        Page<TransactionResponseDTO> dtos = transactions.map(transactionMapper::toResponseDTO);
        return ApiResponse.ok(dtos);
    }

    @PostMapping("/withdrawals/{transactionId}/approve")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> approveWithdrawalTransaction(
            @PathVariable Long transactionId,
            @RequestBody(required = false) Map<String, String> body) {
        Long currentAccountId = SecurityUtils.getAccountId()
                .orElseThrow(() -> new UnauthorizedException("Non authentifié"));
        String note = body != null ? body.get("note") : null;
        withdrawalApprovalService.approveWithdrawal(transactionId, note, currentAccountId);
        return ResponseEntity.ok(ApiResponse.okMessage("Retrait approuvé avec succès"));
    }

    @PostMapping("/withdrawals/{transactionId}/reject")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> rejectWithdrawalTransaction(
            @PathVariable Long transactionId,
            @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        if (reason == null || reason.isBlank()) {
            throw new BadRequestException("Le motif du rejet est obligatoire");
        }
        withdrawalApprovalService.rejectWithdrawal(transactionId, reason);
        return ResponseEntity.ok(ApiResponse.okMessage("Retrait rejeté"));
    }
}