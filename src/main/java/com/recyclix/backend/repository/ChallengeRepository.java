package com.recyclix.backend.repository;

import com.recyclix.backend.model.Challenge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChallengeRepository
        extends JpaRepository<Challenge, Long>,
        JpaSpecificationExecutor<Challenge> {

    // =========================================================
    // 1) Actif / Inactif
    // =========================================================

    List<Challenge> findAllByIsActive(Boolean isActive);

    Page<Challenge> findAllByIsActive(Boolean isActive, Pageable pageable);

    long countByIsActive(Boolean isActive);

    // =========================================================
    // 2) Recherche par titre
    // =========================================================

    Page<Challenge> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

    Optional<Challenge> findByTitleIgnoreCase(String title);

    boolean existsByTitleIgnoreCase(String title);

    // =========================================================
    // 3) Gestion des dates
    // =========================================================

    List<Challenge> findAllByStartDateBefore(LocalDateTime date);

    List<Challenge> findAllByEndDateAfter(LocalDateTime date);

    List<Challenge> findAllByStartDateLessThanEqualAndEndDateGreaterThanEqual(
            LocalDateTime now1,
            LocalDateTime now2
    );

    @Query("""
        select c from Challenge c
        where c.startDate <= :now
          and (c.endDate is null or c.endDate >= :now)
          and c.isActive = true
    """)
    List<Challenge> findCurrentlyActive(@Param("now") LocalDateTime now);

    // =========================================================
    // 4) Création / Dates
    // =========================================================

    List<Challenge> findAllByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    Page<Challenge> findAllByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    // =========================================================
    // 5) Relation UserChallenge (OneToMany)
    // =========================================================

    @EntityGraph(attributePaths = {"userChallenges"})
    Optional<Challenge> findWithUserChallengesById(Long id);

    @Query("""
        select c from Challenge c
        join c.userChallenges uc
        where uc.id = :userChallengeId
    """)
    Optional<Challenge> findByUserChallengeId(@Param("userChallengeId") Long userChallengeId);

    // =========================================================
    // 6) Updates ciblés (optimisation)
    // =========================================================

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Challenge c
        set c.isActive = :status
        where c.id = :id
    """)
    int updateActiveStatus(
            @Param("id") Long id,
            @Param("status") Boolean status
    );

    // =========================================================
    // 7) Lock (éviter conflit modification admin)
    // =========================================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Challenge c where c.id = :id")
    Optional<Challenge> lockById(@Param("id") Long id);

    List<Challenge> findByIsActiveTrue();
}