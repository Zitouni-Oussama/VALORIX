package com.recyclix.backend.dto.expense;

import com.recyclix.backend.model.Expense.ExpenseCategory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseRequestDTO {

    @NotNull(message = "La catégorie ne peut pas être nulle.")
    private ExpenseCategory category;

    @Size(max = 255, message = "La description ne peut pas dépasser 255 caractères.")
    private String description;

    @NotNull(message = "Le montant ne peut pas être nul.")
    private BigDecimal amount;

    // optionnel : si null -> @PrePersist met LocalDate.now()
    private LocalDate expenseDate;

    // optionnel : si tu veux tracer qui a créé (FactoryUser)
    private Long createdById;
}