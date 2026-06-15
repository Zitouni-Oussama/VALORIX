package com.recyclix.backend.repository;

import com.recyclix.backend.model.MaterialPrice;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialPriceRepository
        extends JpaRepository<MaterialPrice, Long>,
        JpaSpecificationExecutor<MaterialPrice> {

    // =========================================================
    // 1) Relation unique Material
    // =========================================================

    Optional<MaterialPrice> findByMaterialId(Long materialId);

    boolean existsByMaterialId(Long materialId);

    // =========================================================
    // 2) Prix actif à une date donnée
    // =========================================================

    @Query("""
        select p from MaterialPrice p
        where p.material.id = :materialId
          and p.startDate <= :date
          and (p.endDate is null or p.endDate >= :date)
    """)
    Optional<MaterialPrice> findActivePrice(
            @Param("materialId") Long materialId,
            @Param("date") LocalDateTime date
    );

    // =========================================================
    // 3) Dates
    // =========================================================

    List<MaterialPrice> findAllByStartDateBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    // =========================================================
    // 4) Lock (modification concurrente prix)
    // =========================================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from MaterialPrice p where p.material.id = :materialId")
    Optional<MaterialPrice> lockByMaterialId(@Param("materialId") Long materialId);

    Optional<MaterialPrice> findCurrentPriceByMaterialId(Long materialId);

    // Alternative si vous avez une date de fin
    @Query("SELECT mp FROM MaterialPrice mp WHERE mp.material.id = :materialId " +
            "AND mp.startDate <= CURRENT_TIMESTAMP " +
            "AND (mp.endDate IS NULL OR mp.endDate >= CURRENT_TIMESTAMP)")
    Optional<MaterialPrice> findActivePriceByMaterialId(@Param("materialId") Long materialId);

    Optional<MaterialPrice> findTopByOrderByCreatedAtDesc();
}