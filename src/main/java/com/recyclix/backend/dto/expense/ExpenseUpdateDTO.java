package com.recyclix.backend.dto.expense;

import com.recyclix.backend.model.Expense.ExpenseCategory;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseUpdateDTO {

    private ExpenseCategory category;

    @Size(max = 255, message = "La description ne peut pas dépasser 255 caractères.")
    private String description;

    private BigDecimal amount;

    private LocalDate expenseDate;

    // optionnel : si tu autorises changer l'auteur (souvent non)
    private Long createdById;
}