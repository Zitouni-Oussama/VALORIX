package com.recyclix.backend.repository;

import com.recyclix.backend.model.Expense;
import com.recyclix.backend.model.Expense.ExpenseCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository
        extends JpaRepository<Expense, Long>,
        JpaSpecificationExecutor<Expense> {

    // =========================================================
    // 1) Category
    // =========================================================

    List<Expense> findAllByCategory(ExpenseCategory category);

    Page<Expense> findAllByCategory(ExpenseCategory category, Pageable pageable);

    long countByCategory(ExpenseCategory category);

    // =========================================================
    // 2) Montant
    // =========================================================

    List<Expense> findAllByAmountGreaterThanEqual(BigDecimal minAmount);

    List<Expense> findAllByAmountBetween(BigDecimal min, BigDecimal max);

    @Query("""
        select sum(e.amount)
        from Expense e
        where e.expenseDate between :start and :end
    """)
    BigDecimal sumAmountBetweenDates(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    // =========================================================
    // 3) Dates
    // =========================================================

    List<Expense> findAllByExpenseDateBetween(
            LocalDate start,
            LocalDate end
    );

    Page<Expense> findAllByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    // =========================================================
    // 4) Relation FactoryUser
    // =========================================================

    List<Expense> findAllByCreatedById(Long factoryUserId);

    @EntityGraph(attributePaths = {"createdBy"})
    Optional<Expense> findWithCreatorById(Long id);

    // =========================================================
    // 5) Lock (modification concurrente)
    // =========================================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Expense e where e.id = :id")
    Optional<Expense> lockById(@Param("id") Long id);

    List<Expense> findByExpenseDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.expenseDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);




}