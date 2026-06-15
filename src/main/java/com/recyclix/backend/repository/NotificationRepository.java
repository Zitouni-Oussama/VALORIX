package com.recyclix.backend.repository;

import com.recyclix.backend.model.Notification;
import com.recyclix.backend.model.Notification.NotificationType;
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
public interface NotificationRepository
        extends JpaRepository<Notification, Long>,
        JpaSpecificationExecutor<Notification> {

    // =========================================================
    // 1) Par utilisateur
    // =========================================================

    List<Notification> findAllByAccountId(Long accountId);

    Page<Notification> findAllByAccountId(Long accountId, Pageable pageable);

    // =========================================================
    // 2) Non lues
    // =========================================================

    List<Notification> findAllByAccountIdAndIsReadFalse(Long accountId);

    long countByAccountIdAndIsReadFalse(Long accountId);

    // =========================================================
    // 3) Type
    // =========================================================

    List<Notification> findAllByAccountIdAndType(
            Long accountId,
            NotificationType type
    );

    // =========================================================
    // 4) Dates
    // =========================================================

    List<Notification> findAllByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    // =========================================================
    // 6) Lock
    // =========================================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select n from Notification n where n.id = :id")
    Optional<Notification> lockById(@Param("id") Long id);

    List<Notification> findByAccountIdOrderByCreatedAtDesc(Long id);

    List<Notification> findAllByAccountIdOrderByCreatedAtDesc(Long accountId);

    // recyclix/backend/repository/NotificationRepository.java

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :id")
    void markAsRead(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.account.id = :accountId")
    void markAllAsRead(@Param("accountId") Long accountId);

    @Query("""
    SELECT n FROM Notification n
    WHERE n.account.id = :accountId
       OR (n.account IS NULL AND (n.targetRole = :role OR n.targetRole = 'ALL'))
    ORDER BY n.createdAt DESC
""")
    List<Notification> findForAccount(@Param("accountId") Long accountId, @Param("role") Notification.RoleTypeN role);
}