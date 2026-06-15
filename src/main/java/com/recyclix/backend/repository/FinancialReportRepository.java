package com.recyclix.backend.repository;

import com.recyclix.backend.model.FinancialReport;
import com.recyclix.backend.model.FinancialReport.ReportType;
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
public interface FinancialReportRepository
        extends JpaRepository<FinancialReport, Long>,
        JpaSpecificationExecutor<FinancialReport> {

    // =========================================================
    // 1) Type (DAILY / MONTHLY / YEARLY)
    // =========================================================

    List<FinancialReport> findAllByReportType(ReportType reportType);

    Page<FinancialReport> findAllByReportType(ReportType reportType, Pageable pageable);

    // =========================================================
    // 2) Période
    // =========================================================

    List<FinancialReport> findAllByPeriodStartGreaterThanEqualAndPeriodEndLessThanEqual(
            LocalDate start,
            LocalDate end
    );

    Optional<FinancialReport> findByReportTypeAndPeriodStartAndPeriodEnd(
            ReportType reportType,
            LocalDate periodStart,
            LocalDate periodEnd
    );

    // =========================================================
    // 3) Profit / Revenue
    // =========================================================

    List<FinancialReport> findAllByNetProfitGreaterThanEqual(BigDecimal minProfit);

    @Query("""
        select sum(f.netProfit)
        from FinancialReport f
        where f.periodStart between :start and :end
    """)
    BigDecimal sumNetProfitBetween(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    // =========================================================
    // 4) Relation FactoryUser
    // =========================================================

    List<FinancialReport> findAllByGeneratedById(Long factoryUserId);

    @EntityGraph(attributePaths = {"generatedBy"})
    Optional<FinancialReport> findWithGeneratorById(Long id);

    // =========================================================
    // 5) Dates génération
    // =========================================================

    List<FinancialReport> findAllByGeneratedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    // =========================================================
    // 6) Lock (génération concurrente)
    // =========================================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from FinancialReport f where f.id = :id")
    Optional<FinancialReport> lockById(@Param("id") Long id);
}