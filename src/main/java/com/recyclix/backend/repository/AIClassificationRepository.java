package com.recyclix.backend.repository;

import com.recyclix.backend.model.AIClassification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AIClassificationRepository
        extends JpaRepository<AIClassification, Long>,
        JpaSpecificationExecutor<AIClassification> {

    // =========================================================
    // 1) Relation OneToOne : CollectionRequest (unique)
    // =========================================================

    Optional<AIClassification> findByRequestId(Long requestId);

    boolean existsByRequestId(Long requestId);

    void deleteByRequestId(Long requestId);

    // =========================================================
    // 2) Validation (isValidated + validatedBy)
    // =========================================================

    List<AIClassification> findAllByIsValidated(Boolean isValidated);

    Page<AIClassification> findAllByIsValidated(Boolean isValidated, Pageable pageable);

    List<AIClassification> findAllByValidatedById(Long factoryUserId);

    Page<AIClassification> findAllByValidatedById(Long factoryUserId, Pageable pageable);

    List<AIClassification> findAllByValidatedByIdAndIsValidatedTrue(Long factoryUserId);

    long countByIsValidated(Boolean isValidated);

    // =========================================================
    // 3) Filtres IA (material / confidence / weight)
    // =========================================================

    List<AIClassification> findAllByPredictedMaterialId(Long materialId);

    Page<AIClassification> findAllByPredictedMaterialId(Long materialId, Pageable pageable);

    List<AIClassification> findAllByConfidenceScoreGreaterThanEqual(BigDecimal minScore);

    List<AIClassification> findAllByPredictedWeightBetween(BigDecimal min, BigDecimal max);

    List<AIClassification> findAllByAiModelVersion(String version);

    // =========================================================
    // 4) Dates
    // =========================================================

    List<AIClassification> findAllByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    Page<AIClassification> findAllByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    // =========================================================
    // 5) EntityGraph (charger relations si nécessaire)
    // =========================================================

    @EntityGraph(attributePaths = {"validatedBy"})
    Optional<AIClassification> findWithFactoryUserById(Long id);

    @EntityGraph(attributePaths = {"request"})
    Optional<AIClassification> findWithRequestById(Long id);

    @EntityGraph(attributePaths = {"validatedBy", "request"})
    Optional<AIClassification> findFullById(Long id);

    // =========================================================
    // 6) Updates ciblés (optimisation)
    // =========================================================

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update AIClassification a
        set a.isValidated = true,
            a.validatedBy.id = :factoryUserId
        where a.id = :id
    """)
    int markValidated(
            @Param("id") Long id,
            @Param("factoryUserId") Long factoryUserId
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update AIClassification a
        set a.confidenceScore = :score
        where a.id = :id
    """)
    int updateConfidenceScore(
            @Param("id") Long id,
            @Param("score") BigDecimal score
    );

    // =========================================================
    // 7) Locking (si validation concurrente)
    // =========================================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AIClassification a where a.id = :id")
    Optional<AIClassification> lockById(@Param("id") Long id);

}