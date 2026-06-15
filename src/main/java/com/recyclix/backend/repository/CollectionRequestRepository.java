package com.recyclix.backend.repository;

import com.recyclix.backend.model.CollectionRequest;
import com.recyclix.backend.model.CollectionRequest.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionRequestRepository
        extends JpaRepository<CollectionRequest, Long>,
        JpaSpecificationExecutor<CollectionRequest> {

    // =========================================================
    // 1) Relation Client
    // =========================================================

    List<CollectionRequest> findAllByClientId(Long clientId);

    Page<CollectionRequest> findAllByClientId(Long clientId, Pageable pageable);

    // =========================================================
    // 2) Relation Material
    // =========================================================

    List<CollectionRequest> findAllByMaterialId(Long materialId);

    // =========================================================
    // 3) Status
    // =========================================================

    List<CollectionRequest> findAllByStatus(Status status);

    Page<CollectionRequest> findAllByStatus(Status status, Pageable pageable);

    long countByStatus(Status status);

    // =========================================================
    // 4) Montants
    // =========================================================

    List<CollectionRequest> findAllByEstimatedQuantityBetween(
            BigDecimal min,
            BigDecimal max
    );

    List<CollectionRequest> findAllByEstimatedAmountGreaterThanEqual(BigDecimal min);

    // =========================================================
    // 5) Dates
    // =========================================================

    List<CollectionRequest> findAllByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    Page<CollectionRequest> findAllByUpdatedAtAfter(
            LocalDateTime date,
            Pageable pageable
    );

    // =========================================================
    // 6) Relations OneToOne
    // =========================================================

    @EntityGraph(attributePaths = {"aiClassification"})
    Optional<CollectionRequest> findWithAIClassificationById(Long id);

    @EntityGraph(attributePaths = {"collection"})
    Optional<CollectionRequest> findWithCollectionById(Long id);

    @EntityGraph(attributePaths = {"aiClassification", "collection"})
    Optional<CollectionRequest> findFullById(Long id);

    // =========================================================
    // 7) Update status
    // =========================================================

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update CollectionRequest r
        set r.status = :status
        where r.id = :id
    """)
    int updateStatus(
            @Param("id") Long id,
            @Param("status") Status status
    );

    // =========================================================
    // 8) Lock (workflow concurrent)
    // =========================================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from CollectionRequest r where r.id = :id")
    Optional<CollectionRequest> lockById(@Param("id") Long id);

    List<CollectionRequest> findAllByClientIdOrderByCreatedAtDesc(Long id);

    // Ajouter ces méthodes
    Optional<CollectionRequest> findByValidationCode(String validationCode);

    boolean existsByValidationCode(String validationCode);

    @Query("SELECT c FROM CollectionRequest c WHERE c.validationCode = :code AND c.codeStatus = :status")
    Optional<CollectionRequest> findByValidationCodeAndCodeStatus(@Param("code") String code, @Param("status") CollectionRequest.CodeStatus status);

    @Modifying
    @Query("UPDATE CollectionRequest c SET c.codeStatus = :status WHERE c.id = :id")
    int updateCodeStatus(@Param("id") Long id, @Param("status") CollectionRequest.CodeStatus status);

}