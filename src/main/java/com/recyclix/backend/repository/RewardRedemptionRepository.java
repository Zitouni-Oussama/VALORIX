package com.recyclix.backend.repository;

import com.recyclix.backend.model.RewardRedemption;
import com.recyclix.backend.model.RewardRedemption.RedemptionStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RewardRedemptionRepository
        extends JpaRepository<RewardRedemption, Long>,
        JpaSpecificationExecutor<RewardRedemption> {

    // =========================================================
    // 1) Par utilisateur
    // =========================================================

    List<RewardRedemption> findAllByAccountIdOrderByCreatedAtDesc(Long accountId);

    Optional<RewardRedemption> findByIdAndAccountId(Long id, Long accountId);

    // =========================================================
    // 2) Par statut
    // =========================================================

    List<RewardRedemption> findAllByStatusOrderByCreatedAtAsc(RedemptionStatus status);

    long countByStatus(RedemptionStatus status);

    // =========================================================
    // 3) Par récompense
    // =========================================================

    List<RewardRedemption> findAllByRewardId(Long rewardId);

    // =========================================================
    // 4) Historique pour comptable
    // =========================================================

    List<RewardRedemption> findAllByReviewedByIdOrderByCreatedAtDesc(Long factoryUserId);

    // =========================================================
    // 5) Requête custom : demandes en attente avec détails
    // =========================================================

    @Query("""
        SELECT rr FROM RewardRedemption rr
        JOIN FETCH rr.account a
        JOIN FETCH rr.reward r
        WHERE rr.status = :status
        ORDER BY rr.createdAt ASC
    """)
    List<RewardRedemption> findPendingWithDetails(@Param("status") RedemptionStatus status);

    // =========================================================
    // 6) Vérification : empêcher les doublons
    // =========================================================

    boolean existsByAccountIdAndRewardIdAndStatus(
            Long accountId,
            Long rewardId,
            RedemptionStatus status
    );
}