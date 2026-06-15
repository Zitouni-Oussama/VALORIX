package com.recyclix.backend.repository;

import com.recyclix.backend.model.Notification;
import com.recyclix.backend.model.SupportTicket;
import com.recyclix.backend.model.SupportTicket.RoleType;
import com.recyclix.backend.model.SupportTicket.Status;
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
public interface SupportTicketRepository
        extends JpaRepository<SupportTicket, Long>,
        JpaSpecificationExecutor<SupportTicket> {

    // =========================================================
    // 1) Par utilisateur
    // =========================================================

    List<SupportTicket> findAllByAccountId(Long accountId);

    Page<SupportTicket> findAllByAccountId(Long accountId, Pageable pageable);

    // =========================================================
    // 2) Par rôle
    // =========================================================

    List<SupportTicket> findAllByRoleType(RoleType roleType);

    // =========================================================
    // 3) Status
    // =========================================================

    List<SupportTicket> findAllByStatus(Status status);

    Page<SupportTicket> findAllByStatus(Status status, Pageable pageable);

    long countByStatus(Status status);

    // =========================================================
    // 4) Assigné par FactoryUser
    // =========================================================

    List<SupportTicket> findAllByCreatedById(Long factoryUserId);

    // =========================================================
    // 5) Dates
    // =========================================================

    List<SupportTicket> findAllByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    // =========================================================
    // 6) Update status + réponse
    // =========================================================

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update SupportTicket t
        set t.status = :status,
            t.responseMessage = :response,
            t.respondedAt = :respondedAt
        where t.id = :id
    """)
    int respondToTicket(
            @Param("id") Long id,
            @Param("status") Status status,
            @Param("response") String response,
            @Param("respondedAt") LocalDateTime respondedAt
    );

    // =========================================================
    // 7) Lock
    // =========================================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from SupportTicket t where t.id = :id")
    Optional<SupportTicket> lockById(@Param("id") Long id);

    Optional<SupportTicket> findByIdAndAccountId(Long ticketId, Long id);

    List<SupportTicket> findByAccountIdOrderByCreatedAtDesc(Long id);

    List<SupportTicket> findAllByAccountIdOrderByCreatedAtDesc(Long id);
}