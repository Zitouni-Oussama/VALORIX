package com.recyclix.backend.repository;

import com.recyclix.backend.model.UserChallenge;
import com.recyclix.backend.model.UserChallenge.ChallengeStatus;
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
public interface UserChallengeRepository extends JpaRepository<UserChallenge, Long>, JpaSpecificationExecutor<UserChallenge> {

    // ============================================================
    // RECHERCHES DE BASE
    // ============================================================

    List<UserChallenge> findAllByAccountId(Long accountId);
    Page<UserChallenge> findAllByAccountId(Long accountId, Pageable pageable);

    List<UserChallenge> findAllByChallengeId(Long challengeId);

    Optional<UserChallenge> findByAccountIdAndChallengeId(Long accountId, Long challengeId);

    boolean existsByAccountIdAndChallengeId(Long accountId, Long challengeId);

    List<UserChallenge> findAllByStatus(ChallengeStatus status);

    // ============================================================
    // NOUVELLES REQUÊTES POUR PROGRESSION AUTO
    // ============================================================

    /**
     * Trouve tous les défis actifs d'un utilisateur (en cours)
     */
    @Query("""
        SELECT uc FROM UserChallenge uc
        JOIN uc.challenge c
        WHERE uc.account.id = :accountId
        AND uc.status = 'IN_PROGRESS'
        AND c.isActive = true
        AND (c.endDate IS NULL OR c.endDate > CURRENT_TIMESTAMP)
    """)
    List<UserChallenge> findActiveChallengesForUser(@Param("accountId") Long accountId);

    /**
     * Trouve les défis spécifiques à un matériau
     */
    @Query("""
        SELECT uc FROM UserChallenge uc
        JOIN uc.challenge c
        WHERE uc.account.id = :accountId
        AND uc.status = 'IN_PROGRESS'
        AND c.challengeType = 'MATERIAL_SPECIFIC'
        AND c.targetMaterialId = :materialId
        AND c.isActive = true
    """)
    List<UserChallenge> findActiveMaterialSpecificChallenges(
            @Param("accountId") Long accountId,
            @Param("materialId") Long materialId
    );

    /**
     * Trouve les défis streak pour vérification quotidienne
     */
    @Query("""
        SELECT uc FROM UserChallenge uc
        JOIN uc.challenge c
        WHERE uc.account.id = :accountId
        AND uc.status = 'IN_PROGRESS'
        AND c.challengeType = 'STREAK_BASED'
        AND c.isActive = true
    """)
    List<UserChallenge> findActiveStreakChallenges(@Param("accountId") Long accountId);

    /**
     * Nombre de défis complétés par un utilisateur
     */
    long countByAccountIdAndStatus(Long accountId, ChallengeStatus status);

    /**
     * Points totaux gagnés via les défis
     */
    @Query("""
        SELECT COALESCE(SUM(c.rewardPoints), 0) FROM UserChallenge uc
        JOIN uc.challenge c
        WHERE uc.account.id = :accountId
        AND uc.status = 'COMPLETED'
    """)
    Integer sumEarnedPointsByAccountId(@Param("accountId") Long accountId);

    // ============================================================
    // LOCK POUR MISE À JOUR CONCURRENTE
    // ============================================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT uc FROM UserChallenge uc WHERE uc.account.id = :accountId AND uc.challenge.id = :challengeId")
    Optional<UserChallenge> lockByAccountAndChallenge(
            @Param("accountId") Long accountId,
            @Param("challengeId") Long challengeId
    );

    Optional<UserChallenge> findByAccountIdAndId(Long id, Long userChallengeId);
}