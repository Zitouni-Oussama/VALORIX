package com.recyclix.backend.service.accountant;

import com.recyclix.backend.controller.accountant.ExpenseController;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.model.Expense;
import com.recyclix.backend.model.FactoryUser;
import com.recyclix.backend.repository.ExpenseRepository;
import com.recyclix.backend.repository.FactoryUserRepository;
import com.recyclix.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseManagementService {

    private final ExpenseRepository expenseRepository;
    private final FactoryUserRepository factoryUserRepository;

    @Transactional
    public Expense recordExpense(
            String description,
            BigDecimal amount,
            Expense.ExpenseCategory category,
            LocalDate date
    ) {
        validateExpense(amount, category);

        FactoryUser createdBy = getCurrentFactoryUser();

        Expense expense = Expense.builder()
                .description(description)
                .amount(amount)
                .category(category)
                .expenseDate(date != null ? date : LocalDate.now())
                .createdBy(createdBy)
                .build();

        return expenseRepository.save(expense);
    }

    @Transactional(readOnly = true)
    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Expense> getExpensesByPeriod(LocalDate startDate, LocalDate endDate) {
        validatePeriod(startDate, endDate);
        return expenseRepository.findByExpenseDateBetween(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalExpenses(LocalDate startDate, LocalDate endDate) {
        validatePeriod(startDate, endDate);

        BigDecimal total = expenseRepository.sumAmountBetweenDates(startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional
    public void deleteExpense(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dépense introuvable."));

        expenseRepository.delete(expense);
    }

    private void validateExpense(BigDecimal amount, Expense.ExpenseCategory category) {
        if (category == null) {
            throw new BadRequestException("La catégorie de la dépense est obligatoire.");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Le montant de la dépense doit être supérieur à zéro.");
        }
    }

    private void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BadRequestException("La date de début et la date de fin sont obligatoires.");
        }

        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("La date de début ne peut pas être après la date de fin.");
        }
    }

    private FactoryUser getCurrentFactoryUser() {
        Long accountId = SecurityUtils.getAccountId()
                .orElseThrow(() -> new BadRequestException("Utilisateur non authentifié."));

        return factoryUserRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Employé usine introuvable."));
    }

    // Dans ExpenseManagementService
    public Expense updateExpense(Long id, ExpenseController.ExpenseRequest request) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dépense introuvable"));
        if (request.getDescription() != null) expense.setDescription(request.getDescription());
        if (request.getAmount() != null) expense.setAmount(request.getAmount());
        if (request.getCategory() != null) expense.setCategory(request.getCategory());
        if (request.getExpenseDate() != null) expense.setExpenseDate(request.getExpenseDate());
        return expenseRepository.save(expense);
    }

    public Expense getExpenseById(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dépense introuvable"));
    }
}