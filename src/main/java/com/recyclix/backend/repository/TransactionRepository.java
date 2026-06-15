package com.recyclix.backend.repository;

import com.recyclix.backend.model.Transaction;
import com.recyclix.backend.model.Transaction.TransactionStatus;
import com.recyclix.backend.model.Transaction.TransactionType;
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
public interface TransactionRepository
        extends JpaRepository<Transaction, Long>,
        JpaSpecificationExecutor<Transaction> {

    // =========================================================
    // 1) Par utilisateur
    // =========================================================

    List<Transaction> findAllByAccountId(Long accountId);

    Page<Transaction> findAllByAccountId(Long accountId, Pageable pageable);

    // =========================================================
    // 2) Type
    // =========================================================

    List<Transaction> findAllByType(TransactionType type);

    // =========================================================
    // 3) Status
    // =========================================================

    List<Transaction> findAllByStatus(TransactionStatus status);

    long countByStatus(TransactionStatus status);

    // =========================================================
    // 4) Relation Collection
    // =========================================================

    Optional<Transaction> findByCollectionId(Long collectionId);

    // =========================================================
    // 5) Montants
    // =========================================================

    List<Transaction> findAllByAmountGreaterThanEqual(BigDecimal amount);

    // =========================================================
    // 6) Dates
    // =========================================================

    List<Transaction> findAllByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    // =========================================================
    // 7) Update status
    // =========================================================

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Transaction t
        set t.status = :status
        where t.id = :id
    """)
    int updateStatus(
            @Param("id") Long id,
            @Param("status") TransactionStatus status
    );

    // =========================================================
    // 8) Lock (critique finance)
    // =========================================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Transaction t where t.id = :id")
    Optional<Transaction> lockById(@Param("id") Long id);

    Long countByAccountId(Long id);


    List<Transaction> findAllByAccountIdOrderByCreatedAtDesc(Long id);

    // repository/TransactionRepository.java
    List<Transaction> findByTypeAndStatus(Transaction.TransactionType type, Transaction.TransactionStatus status);
    Page<Transaction> findByType(Transaction.TransactionType type, Pageable pageable);


}