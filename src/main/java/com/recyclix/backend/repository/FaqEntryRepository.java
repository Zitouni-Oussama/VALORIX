package com.recyclix.backend.repository;

import com.recyclix.backend.model.FaqEntry;
import com.recyclix.backend.model.FaqEntry.RoleType;
import com.recyclix.backend.model.FaqEntry.Status;
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
public interface FaqEntryRepository
        extends JpaRepository<FaqEntry, Long>,
        JpaSpecificationExecutor<FaqEntry> {

    // =========================================================
    // 1) Rôle (CITIZEN / COLLECTOR)
    // =========================================================

    List<FaqEntry> findAllByRoleType(RoleType roleType);

    List<FaqEntry> findAllByRoleTypeAndStatusOrderByDisplayOrderAsc(
            RoleType roleType,
            Status status
    );

    // =========================================================
    // 2) Catégorie
    // =========================================================

    List<FaqEntry> findAllByCategoryKeyAndStatusOrderByDisplayOrderAsc(
            String categoryKey,
            Status status
    );

    List<FaqEntry> findAllByRoleTypeAndCategoryKeyAndStatusOrderByDisplayOrderAsc(
            RoleType roleType,
            String categoryKey,
            Status status
    );

    // =========================================================
    // 3) Status
    // =========================================================

    Page<FaqEntry> findAllByStatus(Status status, Pageable pageable);

    long countByStatus(Status status);

    // =========================================================
    // 4) Recherche texte
    // =========================================================

    Page<FaqEntry> findByQuestionContainingIgnoreCaseOrAnswerContainingIgnoreCase(
            String question,
            String answer,
            Pageable pageable
    );

    // =========================================================
    // 5) Relation FactoryUser
    // =========================================================

    List<FaqEntry> findAllByCreatedById(Long factoryUserId);

    @EntityGraph(attributePaths = {"createdBy"})
    Optional<FaqEntry> findWithCreatorById(Long id);

    // =========================================================
    // 6) Dates
    // =========================================================

    List<FaqEntry> findAllByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    // =========================================================
    // 7) Update ciblé statut
    // =========================================================

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update FaqEntry f
        set f.status = :status
        where f.id = :id
    """)
    int updateStatus(
            @Param("id") Long id,
            @Param("status") Status status
    );

    // =========================================================
    // 8) Lock (admin concurrent)
    // =========================================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from FaqEntry f where f.id = :id")
    Optional<FaqEntry> lockById(@Param("id") Long id);
}