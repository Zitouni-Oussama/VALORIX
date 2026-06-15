package com.recyclix.backend.repository;

import com.recyclix.backend.model.PayrollDeduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PayrollDeductionRepository extends JpaRepository<PayrollDeduction, Long> {

    List<PayrollDeduction> findByEmployeeId(Long employeeId);

    List<PayrollDeduction> findByDeductionDateBetween(LocalDate startDate, LocalDate endDate);


    // Nouvelle méthode : somme des déductions d’un employé
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PayrollDeduction p WHERE p.employee.id = :employeeId")
    BigDecimal sumDeductionsByEmployeeId(@Param("employeeId") Long employeeId);
}