package com.recyclix.backend.repository;

import com.recyclix.backend.model.FactoryValidation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FactoryValidationRepository
        extends JpaRepository<FactoryValidation, Long>,
        JpaSpecificationExecutor<FactoryValidation> {

    // =========================================================
    // 1) Relation Delivery (unique)
    // =========================================================

    Optional<FactoryValidation> findByDeliveryId(Long deliveryId);

    boolean existsByDeliveryId(Long deliveryId);

    // =========================================================
    // 2) Relation FactoryUser
    // =========================================================

    List<FactoryValidation> findAllByValidatedById(Long factoryUserId);

    Page<FactoryValidation> findAllByValidatedById(Long factoryUserId, Pageable pageable);

    // =========================================================
    // 3) Poids
    // =========================================================

    List<FactoryValidation> findAllByValidatedWeightGreaterThanEqual(BigDecimal weight);

    List<FactoryValidation> findAllByDeclaredWeightBetween(BigDecimal min, BigDecimal max);

    // =========================================================
    // 4) Dates
    // =========================================================

    List<FactoryValidation> findAllByValidatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    // =========================================================
    // 5) Relations (EntityGraph)
    // =========================================================

    @EntityGraph(attributePaths = {"delivery"})
    Optional<FactoryValidation> findWithDeliveryById(Long id);

    @EntityGraph(attributePaths = {"validatedBy"})
    Optional<FactoryValidation> findWithValidatorById(Long id);

    @EntityGraph(attributePaths = {"delivery", "validatedBy"})
    Optional<FactoryValidation> findFullById(Long id);

    // =========================================================
    // 6) Lock (validation concurrente)
    // =========================================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from FactoryValidation v where v.id = :id")
    Optional<FactoryValidation> lockById(@Param("id") Long id);


    @Query("SELECT v FROM FactoryValidation v " +
            "JOIN v.delivery d " +
            "JOIN d.collection c " +
            "JOIN c.collector col " +
            "WHERE col.id = :collectorId " +
            "AND v.paid = false " +
            "AND v.rejectionReason IS NULL " +
            "AND d.status IN ('VALIDATED', 'ADJUSTED')")
    List<FactoryValidation> findUnpaidByCollector(@Param("collectorId") Long collectorId);
}