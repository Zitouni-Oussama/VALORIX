package com.recyclix.backend.repository;

import com.recyclix.backend.model.RecyclingCenter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecyclingCenterRepository
        extends JpaRepository<RecyclingCenter, Long>,
        JpaSpecificationExecutor<RecyclingCenter> {

    // =========================================================
    // 1) Recherche
    // =========================================================

    Page<RecyclingCenter> findByNameContainingIgnoreCase(
            String name,
            Pageable pageable
    );

    // =========================================================
    // 2) Capacité
    // =========================================================

    List<RecyclingCenter> findAllByCapacityGreaterThanEqual(BigDecimal capacity);

    // =========================================================
    // 3) Dates
    // =========================================================

    List<RecyclingCenter> findAllByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    // =========================================================
    // 4) Relations
    // =========================================================

    @EntityGraph(attributePaths = {"deliveries"})
    Optional<RecyclingCenter> findWithDeliveriesById(Long id);

    // =========================================================
    // 5) Lock
    // =========================================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RecyclingCenter r where r.id = :id")
    Optional<RecyclingCenter> lockById(Long id);
}