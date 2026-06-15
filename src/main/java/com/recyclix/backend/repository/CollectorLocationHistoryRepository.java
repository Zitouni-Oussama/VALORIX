package com.recyclix.backend.repository;

import com.recyclix.backend.model.CollectorLocationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CollectorLocationHistoryRepository
        extends JpaRepository<CollectorLocationHistory, Long> {

    // =========================================================
    // 1) POSITION ACTUELLE (la plus importante)
    // =========================================================

    // Dernière position du collecteur (celle que tu utilises dans ton service)
    Optional<CollectorLocationHistory> findTopByCollectorIdOrderByRecordedAtDesc(Long collectorId);


    // =========================================================
    // 2) HISTORIQUE DU COLLECTEUR
    // =========================================================

    // Toutes les positions d’un collecteur (triées)
    List<CollectorLocationHistory> findAllByCollectorIdOrderByRecordedAtDesc(Long collectorId);

    // Avec pagination possible plus tard (optionnel)
    // Page<CollectorLocationHistory> findAllByCollectorId(Long collectorId, Pageable pageable);


    // =========================================================
    // 3) FILTRE PAR PÉRIODE
    // =========================================================

    // Positions entre deux dates
    List<CollectorLocationHistory> findAllByCollectorIdAndRecordedAtBetween(
            Long collectorId,
            LocalDateTime start,
            LocalDateTime end
    );

    // Positions après une date
    List<CollectorLocationHistory> findAllByCollectorIdAndRecordedAtAfter(
            Long collectorId,
            LocalDateTime date
    );


    // =========================================================
    // 4) EXISTENCE / CHECK
    // =========================================================

    // Vérifier si le collecteur a déjà envoyé une position
    boolean existsByCollectorId(Long collectorId);


    // =========================================================
    // 5) SUPPRESSION (utile admin / nettoyage)
    // =========================================================

    // Supprimer historique d’un collecteur
    void deleteAllByCollectorId(Long collectorId);


    // =========================================================
    // 6) DERNIÈRE POSITION AVANT UNE DATE (optionnel avancé)
    // =========================================================

    Optional<CollectorLocationHistory> findTopByCollectorIdAndRecordedAtLessThanEqualOrderByRecordedAtDesc(
            Long collectorId,
            LocalDateTime date
    );
}