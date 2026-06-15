package com.recyclix.backend.repository;

import com.recyclix.backend.model.FactoryInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface FactoryInvoiceRepository extends JpaRepository<FactoryInvoice, Long>, JpaSpecificationExecutor<FactoryInvoice> {

    List<FactoryInvoice> findByStatus(FactoryInvoice.InvoiceStatus status);

    List<FactoryInvoice> findByStatusAndDueDateBefore(FactoryInvoice.InvoiceStatus status, LocalDate date);

    // ✅ CORRIGÉ : amount_ht au lieu de amount
    @Query("SELECT COALESCE(SUM(f.amountHt), 0) FROM FactoryInvoice f WHERE f.status IN :statuses")
    BigDecimal sumAmountByStatuses(@Param("statuses") List<FactoryInvoice.InvoiceStatus> statuses);

    // ✅ CORRIGÉ : amount_ht au lieu de amount
    @Query("SELECT COALESCE(SUM(f.amountHt), 0) FROM FactoryInvoice f WHERE f.status IN :statuses")
    BigDecimal sumAmountHtByStatuses(@Param("statuses") List<FactoryInvoice.InvoiceStatus> statuses);

    // ✅ CORRIGÉ : amount_ttc au lieu de amount
    @Query("SELECT COALESCE(SUM(f.amountTtc), 0) FROM FactoryInvoice f WHERE f.status IN :statuses")
    BigDecimal sumAmountTtcByStatuses(@Param("statuses") List<FactoryInvoice.InvoiceStatus> statuses);

    // ✅ CORRIGÉ : issueDate et amount_ht
    @Query("SELECT COALESCE(SUM(f.amountHt), 0) FROM FactoryInvoice f " +
            "WHERE f.status IN :statuses AND f.issueDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountHtByStatusesAndIssueDateBetween(
            @Param("statuses") List<FactoryInvoice.InvoiceStatus> statuses,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // ✅ CORRIGÉ : issueDate et amount_ttc
    @Query("SELECT COALESCE(SUM(f.amountTtc), 0) FROM FactoryInvoice f " +
            "WHERE f.status IN :statuses AND f.issueDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountTtcByStatusesAndIssueDateBetween(
            @Param("statuses") List<FactoryInvoice.InvoiceStatus> statuses,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    Long countByStatus(FactoryInvoice.InvoiceStatus status);

    @Query("SELECT COUNT(f) FROM FactoryInvoice f WHERE f.issueDate BETWEEN :startDate AND :endDate")
    int countByIssueDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);


}