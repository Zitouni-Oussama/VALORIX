package com.recyclix.backend.repository;

import com.recyclix.backend.model.PointMovement;
import com.recyclix.backend.model.PointMovement.PointMovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PointMovementRepository
        extends JpaRepository<PointMovement, Long>,
        JpaSpecificationExecutor<PointMovement> {

    // =========================================================
    // 1) Par utilisateur
    // =========================================================

    List<PointMovement> findAllByAccountId(Long accountId);

    Page<PointMovement> findAllByAccountId(Long accountId, Pageable pageable);

    // =========================================================
    // 2) Type
    // =========================================================

    List<PointMovement> findAllByType(PointMovementType type);

    // =========================================================
    // 3) Relation Collection
    // =========================================================

    Optional<PointMovement> findByCollectionId(Long collectionId);

    // =========================================================
    // 4) Dates
    // =========================================================

    List<PointMovement> findAllByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    // =========================================================
    // 5) Lock (ajout points concurrent)
    // =========================================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PointMovement p where p.id = :id")
    Optional<PointMovement> lockById(Long id);

    List<PointMovement> findAllByAccountIdOrderByCreatedAtDesc(Long accountId);

    long countByAccountId(Long accountId);

}