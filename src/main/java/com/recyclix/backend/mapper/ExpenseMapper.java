package com.recyclix.backend.mapper;

import com.recyclix.backend.dto.expense.ExpenseRequestDTO;
import com.recyclix.backend.dto.expense.ExpenseResponseDTO;
import com.recyclix.backend.dto.expense.ExpenseSummaryDTO;
import com.recyclix.backend.dto.expense.ExpenseUpdateDTO;
import com.recyclix.backend.model.Expense;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ExpenseMapper {

    // =========================
    // Entity -> Response DTO
    // =========================
    @Mapping(target = "createdById", expression = "java(entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null)")
    ExpenseResponseDTO toDto(Expense entity);

    // =========================
    // Entity -> Summary DTO
    // =========================
    @Mapping(target = "createdById", expression = "java(entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null)")
    ExpenseSummaryDTO toSummaryDto(Expense entity);

    // =========================
    // Request DTO -> Entity
    // =========================
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),

            // Relation gérée dans le service
            @Mapping(target = "createdBy", ignore = true)
    })
    Expense toEntity(ExpenseRequestDTO dto);

    // =========================
    // Update DTO -> existing Entity
    // =========================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),

            @Mapping(target = "createdBy", ignore = true)
    })
    void updateEntityFromDto(ExpenseUpdateDTO dto, @MappingTarget Expense entity);
}