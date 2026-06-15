package com.recyclix.backend.dto.expense;

import com.recyclix.backend.model.Expense.ExpenseCategory;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseSummaryDTO {

    private Long id;

    private ExpenseCategory category;
    private BigDecimal amount;

    private LocalDate expenseDate;
    private LocalDateTime createdAt;

    private Long createdById;
}