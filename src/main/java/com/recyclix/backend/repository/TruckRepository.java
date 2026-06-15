package com.recyclix.backend.repository;

import com.recyclix.backend.model.Truck;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TruckRepository
        extends JpaRepository<Truck, Long>,
        JpaSpecificationExecutor<Truck> {

    // =========================================================
    // 1) Par collecteur (unique)
    // =========================================================

    Optional<Truck> findByCollectorId(Long collectorId);

    boolean existsByCollectorId(Long collectorId);

    // =========================================================
    // 2) Plaque unique
    // =========================================================

    Optional<Truck> findByPlateNumber(String plateNumber);

    boolean existsByPlateNumber(String plateNumber);

    // =========================================================
    // 3) Actif / Inactif
    // =========================================================

    List<Truck> findAllByIsActive(Boolean active);

    // =========================================================
    // 4) Dates
    // =========================================================

    List<Truck> findAllByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    // =========================================================
    // 5) Update activation
    // =========================================================

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Truck t
        set t.isActive = :status
        where t.id = :id
    """)
    int updateActiveStatus(
            @Param("id") Long id,
            @Param("status") Boolean status
    );

    // =========================================================
    // 6) Lock
    // =========================================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Truck t where t.id = :id")
    Optional<Truck> lockById(@Param("id") Long id);
}