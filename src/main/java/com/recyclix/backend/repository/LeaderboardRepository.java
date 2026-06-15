package com.recyclix.backend.repository;

import com.recyclix.backend.model.Leaderboard;
import com.recyclix.backend.model.Leaderboard.PeriodType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeaderboardRepository
        extends JpaRepository<Leaderboard, Long>,
        JpaSpecificationExecutor<Leaderboard> {

    // =========================================================
    // 1) Par période
    // =========================================================

    List<Leaderboard> findAllByPeriodTypeAndPeriodStartAndPeriodEnd(
            PeriodType periodType,
            LocalDate start,
            LocalDate end
    );

    Page<Leaderboard> findAllByPeriodType(
            PeriodType periodType,
            Pageable pageable
    );

    // =========================================================
    // 2) Classement par période (trié)
    // =========================================================

    List<Leaderboard> findAllByPeriodTypeAndPeriodStartAndPeriodEndOrderByRankPositionAsc(
            PeriodType periodType,
            LocalDate start,
            LocalDate end
    );

    // =========================================================
    // 3) Compte utilisateur
    // =========================================================

    List<Leaderboard> findAllByClientId(Long ClientId);

    Optional<Leaderboard> findByClientIdAndPeriodTypeAndPeriodStartAndPeriodEnd(
            Long ClientId,
            PeriodType periodType,
            LocalDate start,
            LocalDate end
    );

    // =========================================================
    // 4) Statistiques
    // =========================================================

    long countByPeriodTypeAndPeriodStartAndPeriodEnd(
            PeriodType periodType,
            LocalDate start,
            LocalDate end
    );

    // =========================================================
    // 5) Lock (génération classement)
    // =========================================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select l from Leaderboard l
        where l.periodType = :type
          and l.periodStart = :start
          and l.periodEnd = :end
    """)
    List<Leaderboard> lockPeriod(
            @Param("type") PeriodType type,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
}