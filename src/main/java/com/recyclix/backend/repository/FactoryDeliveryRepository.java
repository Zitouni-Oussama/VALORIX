package com.recyclix.backend.repository;

import com.recyclix.backend.model.FactoryDelivery;
import com.recyclix.backend.model.FactoryDelivery.DeliveryStatus;
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
public interface FactoryDeliveryRepository
        extends JpaRepository<FactoryDelivery, Long>,
        JpaSpecificationExecutor<FactoryDelivery> {

    // =========================================================
    // 1) Relation Collection (unique)
    // =========================================================

    Optional<FactoryDelivery> findByCollectionId(Long collectionId);

    boolean existsByCollectionId(Long collectionId);

    // =========================================================
    // 2) Status
    // =========================================================

    List<FactoryDelivery> findAllByStatus(DeliveryStatus status);

    Page<FactoryDelivery> findAllByStatus(DeliveryStatus status, Pageable pageable);

    long countByStatus(DeliveryStatus status);

    // =========================================================
    // 3) Dates
    // =========================================================

    List<FactoryDelivery> findAllByDeliveryDateBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    List<FactoryDelivery> findAllByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    // =========================================================
    // 4) Relations (EntityGraph)
    // =========================================================

    @EntityGraph(attributePaths = {"collection"})
    Optional<FactoryDelivery> findWithCollectionById(Long id);

    @EntityGraph(attributePaths = {"validation"})
    Optional<FactoryDelivery> findWithValidationById(Long id);

    @EntityGraph(attributePaths = {"recyclingCenter"})
    Optional<FactoryDelivery> findWithRecyclingCenterById(Long id);

    @EntityGraph(attributePaths = {"collection", "validation", "recyclingCenter"})
    Optional<FactoryDelivery> findFullById(Long id);

    // =========================================================
    // 5) Update Status
    // =========================================================

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update FactoryDelivery f
        set f.status = :status
        where f.id = :id
    """)
    int updateStatus(
            @Param("id") Long id,
            @Param("status") DeliveryStatus status
    );

    // =========================================================
    // 6) Lock (validation concurrente)
    // =========================================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from FactoryDelivery f where f.id = :id")
    Optional<FactoryDelivery> lockById(@Param("id") Long id);
}