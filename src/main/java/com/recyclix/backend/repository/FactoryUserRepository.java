package com.recyclix.backend.repository;

import com.recyclix.backend.model.Account;
import com.recyclix.backend.model.FactoryUser;
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
public interface FactoryUserRepository
        extends JpaRepository<FactoryUser, Long>,
        JpaSpecificationExecutor<FactoryUser> {

    // =========================================================
    // 1) Relation Account (unique)
    // =========================================================

    Optional<FactoryUser> findByAccountId(Long accountId);

    boolean existsByAccountId(Long accountId);

    void deleteByAccountId(Long accountId);

    // =========================================================
    // 2) Employee number
    // =========================================================

    Optional<FactoryUser> findByEmployeeNumber(String employeeNumber);

    boolean existsByEmployeeNumber(String employeeNumber);

    // =========================================================
    // 3) Recherche simple
    // =========================================================

    Page<FactoryUser> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName,
            String lastName,
            Pageable pageable
    );

    Page<FactoryUser> findByPositionContainingIgnoreCase(
            String position,
            Pageable pageable
    );

    // =========================================================
    // 4) Dates
    // =========================================================

    List<FactoryUser> findAllByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    Page<FactoryUser> findAllByUpdatedAtAfter(
            LocalDateTime date,
            Pageable pageable
    );

    // =========================================================
    // 5) Relations (EntityGraph)
    // =========================================================

    @EntityGraph(attributePaths = {"validations"})
    Optional<FactoryUser> findWithValidationsById(Long id);

    @EntityGraph(attributePaths = {"expenses"})
    Optional<FactoryUser> findWithExpensesById(Long id);

    @EntityGraph(attributePaths = {"financialReports"})
    Optional<FactoryUser> findWithReportsById(Long id);

    @EntityGraph(attributePaths = {
            "validations",
            "expenses",
            "financialReports",
            "recordedAbsences",
            "reportedIncidents"
    })
    Optional<FactoryUser> findFullProfileById(Long id);

    // =========================================================
    // 6) Lock
    // =========================================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from FactoryUser f where f.id = :id")
    Optional<FactoryUser> lockById(@Param("id") Long id);


    Page<FactoryUser> findAllByPosition(FactoryUser.FactoryPosition position, Pageable pageable);

    Optional<FactoryUser> findByAccount_Email(String email);
}