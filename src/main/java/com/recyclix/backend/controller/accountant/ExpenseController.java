package com.recyclix.backend.controller.accountant;

import com.recyclix.backend.model.Expense;
import com.recyclix.backend.service.accountant.ExpenseManagementService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/accountant/expenses")
@PreAuthorize("@factoryAccess.hasPosition('ACCOUNTANT')")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseManagementService expenseManagementService;

    @PostMapping
    public ResponseEntity<Expense> addExpense(@RequestBody ExpenseRequest request) {
        Expense newExpense = expenseManagementService.recordExpense(
                request.getDescription(),
                request.getAmount(),
                request.getCategory(),
                request.getExpenseDate()
        );
        return ResponseEntity.ok(newExpense);
    }

    @GetMapping
    public ResponseEntity<List<Expense>> getAllExpenses() {
        return ResponseEntity.ok(expenseManagementService.getAllExpenses());
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Expense>> getExpensesByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(expenseManagementService.getExpensesByPeriod(startDate, endDate));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteExpense(@PathVariable Long id) {
        expenseManagementService.deleteExpense(id);
        return ResponseEntity.ok("Dépense supprimée avec succès.");
    }

    @Data
    public static class ExpenseRequest {
        private String description;
        private BigDecimal amount;
        private Expense.ExpenseCategory category;
        private LocalDate expenseDate;
    }
}