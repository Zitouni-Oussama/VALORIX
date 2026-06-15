// repository/CancellationLogRepository.java
package com.recyclix.backend.repository;

import com.recyclix.backend.model.CancellationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CancellationLogRepository extends JpaRepository<CancellationLog, Long> {

    // Compter les annulations d'un compte sur une période
    long countByAccountIdAndCancelledAtAfter(Long accountId, LocalDateTime since);

    // Récupérer les logs récents d'un compte
    List<CancellationLog> findByAccountIdOrderByCancelledAtDesc(Long accountId);

    // Supprimer les logs anciens (optionnel pour nettoyage)
    void deleteByCancelledAtBefore(LocalDateTime date);
}