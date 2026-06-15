package com.recyclix.backend.repository;

import com.recyclix.backend.model.MachineIncident;
import com.recyclix.backend.model.MachineIncident.IncidentSeverity;
import com.recyclix.backend.model.MachineIncident.IncidentStatus;
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
public interface MachineIncidentRepository
        extends JpaRepository<MachineIncident, Long>,
        JpaSpecificationExecutor<MachineIncident> {

    // =========================================================
    // 1) Par machine
    // =========================================================

    List<MachineIncident> findAllByMachineId(Long machineId);

    Page<MachineIncident> findAllByMachineId(Long machineId, Pageable pageable);

    // =========================================================
    // 2) Status
    // =========================================================

    List<MachineIncident> findAllByStatus(IncidentStatus status);

    List<MachineIncident> findAllBySeverity(IncidentSeverity severity);

    // =========================================================
    // 3) Reporté par
    // =========================================================

    List<MachineIncident> findAllByReportedById(Long factoryUserId);

    // =========================================================
    // 4) Dates
    // =========================================================

    List<MachineIncident> findAllByReportedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    List<MachineIncident> findAllByResolvedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    // =========================================================
    // 5) Relations
    // =========================================================

    @EntityGraph(attributePaths = {"machine", "reportedBy"})
    Optional<MachineIncident> findFullById(Long id);

    // =========================================================
    // 6) Update status
    // =========================================================

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update MachineIncident i
        set i.status = :status
        where i.id = :id
    """)
    int updateStatus(
            @Param("id") Long id,
            @Param("status") IncidentStatus status
    );

    // =========================================================
    // 7) Lock
    // =========================================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from MachineIncident i where i.id = :id")
    Optional<MachineIncident> lockById(@Param("id") Long id);

    Page<MachineIncident> findAllBySeverity(MachineIncident.IncidentSeverity severity, Pageable pageable);
    Page<MachineIncident> findAllByStatus(MachineIncident.IncidentStatus status, Pageable pageable);
}