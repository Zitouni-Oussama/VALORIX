package com.recyclix.backend.repository;

import com.recyclix.backend.model.Reward;
import com.recyclix.backend.model.Reward.RewardCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface RewardRepository
        extends JpaRepository<Reward, Long>,
        JpaSpecificationExecutor<Reward> {

    // =========================================================
    // 1) Actif / Disponible
    // =========================================================

    /**
     * Récompenses actives et en stock (stock != 0)
     */
    @Query("""
        SELECT r FROM Reward r
        WHERE r.isActive = true
          AND (r.stock = -1 OR r.stock > 0)
        ORDER BY r.category, r.name
    """)
    List<Reward> findAllAvailable();

    /**
     * Récompenses actives seulement
     */
    List<Reward> findAllByIsActiveTrue();

    // =========================================================
    // 2) Par catégorie
    // =========================================================

    @Query("""
        SELECT r FROM Reward r
        WHERE r.category = :category
          AND r.isActive = true
          AND (r.stock = -1 OR r.stock > 0)
        ORDER BY r.name
    """)
    List<Reward> findAvailableByCategory(@Param("category") RewardCategory category);

    List<Reward> findAllByCategory(RewardCategory category);

    // =========================================================
    // 3) Recherche par nom
    // =========================================================

    Page<Reward> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    // =========================================================
    // 4) Partenaire
    // =========================================================

    List<Reward> findAllByPartnerNameIgnoreCase(String partnerName);

    // =========================================================
    // 5) Admin : tous avec pagination
    // =========================================================

    @Query("""
        SELECT r FROM Reward r
        ORDER BY r.isActive DESC, r.category, r.pointsCost
    """)
    Page<Reward> findAllForAdmin(Pageable pageable);

    // =========================================================
    // 6) Lock (pour décrémenter le stock en sécurité)
    // =========================================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reward r WHERE r.id = :id")
    Optional<Reward> lockById(@Param("id") Long id);
}