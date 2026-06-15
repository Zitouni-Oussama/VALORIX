package com.recyclix.backend.repository;

import com.recyclix.backend.model.Client;
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
public interface ClientRepository
        extends JpaRepository<Client, Long>,
        JpaSpecificationExecutor<Client> {

    // =========================================================
    // 1) Relation Account (OneToOne unique)
    // =========================================================

    Optional<Client> findByAccountId(Long accountId);

    boolean existsByAccountId(Long accountId);

    void deleteByAccountId(Long accountId);

    // =========================================================
    // 2) Recherche simple
    // =========================================================

    Page<Client> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName,
            String lastName,
            Pageable pageable
    );

    List<Client> findAllByTotalPointsGreaterThanEqual(Integer minPoints);

    Page<Client> findAllByTotalPointsGreaterThanEqual(Integer minPoints, Pageable pageable);

    // =========================================================
    // 3) Localisation (si tu fais géolocalisation)
    // =========================================================

    List<Client> findAllByLatitudeBetweenAndLongitudeBetween(
            BigDecimal minLat,
            BigDecimal maxLat,
            BigDecimal minLng,
            BigDecimal maxLng
    );

    // =========================================================
    // 4) Dates
    // =========================================================

    List<Client> findAllByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    Page<Client> findAllByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    // =========================================================
    // 5) Relation CollectionRequest (OneToMany)
    // =========================================================

    @EntityGraph(attributePaths = {"collectionRequests"})
    Optional<Client> findWithCollectionRequestsById(Long id);

    @Query("""
        select c from Client c
        join c.collectionRequests cr
        where cr.id = :requestId
    """)
    Optional<Client> findByCollectionRequestId(@Param("requestId") Long requestId);

    // =========================================================
    // 6) Gestion des points (optimisation)
    // =========================================================

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Client c
        set c.totalPoints = :points
        where c.id = :id
    """)
    int updateTotalPoints(
            @Param("id") Long id,
            @Param("points") Integer points
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Client c
        set c.totalPoints = c.totalPoints + :increment
        where c.id = :id
    """)
    int incrementPoints(
            @Param("id") Long id,
            @Param("increment") Integer increment
    );

    // =========================================================
    // 7) Lock (si mise à jour concurrente des points)
    // =========================================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Client c where c.id = :id")
    Optional<Client> lockById(@Param("id") Long id);

}