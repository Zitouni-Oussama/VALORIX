package com.recyclix.backend.repository;

import com.recyclix.backend.model.Machine;
import com.recyclix.backend.model.Machine.MachineStatus;
import com.recyclix.backend.model.MachineIncident;
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
public interface MachineRepository
        extends JpaRepository<Machine, Long>,
        JpaSpecificationExecutor<Machine> {

    // =========================================================
    // 1) Serial unique
    // =========================================================

    Optional<Machine> findBySerialNumber(String serialNumber);

    boolean existsBySerialNumber(String serialNumber);

    // =========================================================
    // 2) Status
    // =========================================================

    List<Machine> findAllByStatus(MachineStatus status);

    Page<Machine> findAllByStatus(MachineStatus status, Pageable pageable);

    // =========================================================
    // 3) Dates
    // =========================================================

    List<Machine> findAllByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    // =========================================================
    // 4) Relations
    // =========================================================

    @EntityGraph(attributePaths = {"incidents"})
    Optional<Machine> findWithIncidentsById(Long id);

    // =========================================================
    // 5) Update status
    // =========================================================

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Machine m
        set m.status = :status
        where m.id = :id
    """)
    int updateStatus(
            @Param("id") Long id,
            @Param("status") MachineStatus status
    );

    // =========================================================
    // 6) Lock
    // =========================================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Machine m where m.id = :id")
    Optional<Machine> lockById(@Param("id") Long id);


    long countByStatus(Machine.MachineStatus status);

}