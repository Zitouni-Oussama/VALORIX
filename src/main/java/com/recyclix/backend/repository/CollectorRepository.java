package com.recyclix.backend.repository;

import com.recyclix.backend.model.Collector;
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
public interface CollectorRepository
        extends JpaRepository<Collector, Long>,
        JpaSpecificationExecutor<Collector> {

    // =========================================================
    // 1) Relation Account (unique)
    // =========================================================

    Optional<Collector> findByAccountId(Long accountId);

    boolean existsByAccountId(Long accountId);

    void deleteByAccountId(Long accountId);

    // =========================================================
    // 2) Vérification
    // =========================================================

    List<Collector> findAllByIsVerified(Boolean verified);

    Page<Collector> findAllByIsVerified(Boolean verified, Pageable pageable);

    long countByIsVerified(Boolean verified);

    // =========================================================
    // 3) Recherche simple
    // =========================================================

    Page<Collector> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName,
            String lastName,
            Pageable pageable
    );

    Optional<Collector> findByNationalIdNumber(String nationalIdNumber);

    boolean existsByNationalIdNumber(String nationalIdNumber);

    // =========================================================
    // 4) Rating
    // =========================================================

    List<Collector> findAllByAverageRatingGreaterThanEqual(BigDecimal rating);

    Page<Collector> findAllByAverageRatingGreaterThanEqual(BigDecimal rating, Pageable pageable);

    // =========================================================
    // 5) Localisation actuelle
    // =========================================================

    List<Collector> findAllByCurrentLatitudeBetweenAndCurrentLongitudeBetween(
            BigDecimal minLat,
            BigDecimal maxLat,
            BigDecimal minLng,
            BigDecimal maxLng
    );

    // =========================================================
    // 6) Dates
    // =========================================================

    List<Collector> findAllByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    // =========================================================
    // 7) Relations (EntityGraph)
    // =========================================================

    @EntityGraph(attributePaths = {"truck"})
    Optional<Collector> findWithTruckById(Long id);

    @EntityGraph(attributePaths = {"collectorLocationHistory"})
    Optional<Collector> findWithLocationHistoryById(Long id);

    @EntityGraph(attributePaths = {"collections"})
    Optional<Collector> findWithCollectionsById(Long id);

    @EntityGraph(attributePaths = {"truck", "collectorLocationHistory"})
    Optional<Collector> findFullProfileById(Long id);

    // =========================================================
    // 8) Update ciblé
    // =========================================================

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Collector c
        set c.isVerified = :verified
        where c.id = :id
    """)
    int updateVerificationStatus(
            @Param("id") Long id,
            @Param("verified") Boolean verified
    );

//    @Modifying(clearAutomatically = true, flushAutomatically = true)
//    @Query("""
//        update Collector c
//        set c.averageRating = :rating
//        where c.id = :id
//    """)
//    int updateRating(
//            @Param("id") Long id,
//            @Param("rating") BigDecimal rating
//    );

    // =========================================================
    // 9) Lock (ex: mise à jour rating concurrente)
    // =========================================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Collector c where c.id = :id")
    Optional<Collector> lockById(@Param("id") Long id);

    // Déjà présent dans votre code :
    @Modifying
    @Query("update Collector c set c.averageRating = :rating where c.id = :id")
    int updateRating(@Param("id") Long id, @Param("rating") BigDecimal rating);

    List<Collector> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrAccount_EmailContainingIgnoreCase(
            String firstName, String lastName, String email);
}