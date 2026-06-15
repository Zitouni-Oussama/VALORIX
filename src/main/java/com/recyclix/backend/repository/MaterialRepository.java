package com.recyclix.backend.repository;

import com.recyclix.backend.model.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialRepository
        extends JpaRepository<Material, Long>,
        JpaSpecificationExecutor<Material> {

    // =========================================================
    // 1) Nom unique
    // =========================================================

    Optional<Material> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    // =========================================================
    // 2) Activation
    // =========================================================

    List<Material> findAllByIsActive(Boolean active);

    Page<Material> findAllByIsActive(Boolean active, Pageable pageable);

    long countByIsActive(Boolean active);

    // =========================================================
    // 3) Dates
    // =========================================================

    List<Material> findAllByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    // =========================================================
    // 4) Relations
    // =========================================================

    @EntityGraph(attributePaths = {"materialPrice"})
    Optional<Material> findWithPriceById(Long id);

    @EntityGraph(attributePaths = {"collectionRequests"})
    Optional<Material> findWithRequestsById(Long id);

    // =========================================================
    // 5) Update activation
    // =========================================================

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Material m
        set m.isActive = :status
        where m.id = :id
    """)
    int updateActiveStatus(
            @Param("id") Long id,
            @Param("status") Boolean status
    );

    // =========================================================
    // 6) Lock
    // =========================================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Material m where m.id = :id")
    Optional<Material> lockById(@Param("id") Long id);
}