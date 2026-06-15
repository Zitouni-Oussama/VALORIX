package com.recyclix.backend.repository;

import com.recyclix.backend.model.Collection;
import com.recyclix.backend.model.Collection.PaymentStatus;
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
public interface CollectionRepository
        extends JpaRepository<Collection, Long>,
        JpaSpecificationExecutor<Collection> {

    // =========================================================
    // 1) Relation unique avec CollectionRequest
    // =========================================================

    Optional<Collection> findByRequestId(Long requestId);

    boolean existsByRequestId(Long requestId);

    // =========================================================
    // 2) Relation Collector
    // =========================================================

//    List<Collection> findAllByCollectorId(Long collectorId);

    Page<Collection> findAllByCollectorId(Long collectorId, Pageable pageable);

    // =========================================================
    // 3) PaymentStatus
    // =========================================================

    List<Collection> findAllByPaymentStatus(PaymentStatus status);

    Page<Collection> findAllByPaymentStatus(PaymentStatus status, Pageable pageable);

    long countByPaymentStatus(PaymentStatus status);

    // =========================================================
    // 4) Montants / Quantité
    // =========================================================

    List<Collection> findAllByRealQuantityBetween(
            BigDecimal min,
            BigDecimal max
    );

    List<Collection> findAllByTotalAmountGreaterThanEqual(BigDecimal minAmount);

    // =========================================================
    // 5) Dates
    // =========================================================

    List<Collection> findAllByCollectedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    Page<Collection> findAllByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    // =========================================================
    // 6) EntityGraph (charger relations si nécessaire)
    // =========================================================

    @EntityGraph(attributePaths = {"collector"})
    Optional<Collection> findWithCollectorById(Long id);

    @EntityGraph(attributePaths = {"factoryDelivery", "transaction", "pointMovement"})
    Optional<Collection> findFullById(Long id);

    // =========================================================
    // 7) Update ciblé paiement
    // =========================================================

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                update Collection c
                set c.paymentStatus = :status
                where c.id = :id
            """)
    int updatePaymentStatus(
            @Param("id") Long id,
            @Param("status") PaymentStatus status
    );

    // =========================================================
    // 8) Lock (paiement concurrent)
    // =========================================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Collection c where c.id = :id")
    Optional<Collection> lockById(@Param("id") Long id);

    @Query("""
                select c
                from Collection c
                join c.request r
                join r.client cl
                left join r.material m
                where cl.id = :clientId
                  and c.collectedAt >= :joinedAt
                  and (:materialType is null or lower(m.name) = lower(:materialType))
            """)
    List<Collection> findCompletedCollectionsForChallenge(
            @Param("clientId") Long clientId,
            @Param("joinedAt") LocalDateTime joinedAt,
            @Param("materialType") String materialType
    );


    @Query("""
                SELECT COALESCE(SUM(c.realQuantity * c.unitPriceFrozen * 0.125), 0)
                FROM Collection c
                WHERE c.collectedAt BETWEEN :startDate AND :endDate
            """)
    BigDecimal sumEstimatedPointCostsByCollectedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
                SELECT COALESCE(SUM(c.realQuantity), 0)
                FROM Collection c
                WHERE c.collectedAt BETWEEN :startDate AND :endDate
            """)
    BigDecimal sumCollectedKgByCollectedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // À AJOUTER dans l'interface CollectionRepository
    @Query("SELECT c FROM Collection c WHERE c.collector.id = :collectorId")
    List<Collection> findAllByCollectorId(@Param("collectorId") Long collectorId);
}