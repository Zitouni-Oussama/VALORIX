package com.recyclix.backend.repository;

import com.recyclix.backend.model.Payment;
import com.recyclix.backend.model.Payment.PaymentMethod;
import com.recyclix.backend.model.Payment.PaymentStatus;
import com.recyclix.backend.model.PointMovement;
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
public interface PaymentRepository
        extends JpaRepository<Payment, Long>,
        JpaSpecificationExecutor<Payment> {

    // =========================================================
    // 1) Par utilisateur
    // =========================================================

    List<Payment> findAllByAccountId(Long accountId);

    Page<Payment> findAllByAccountId(Long accountId, Pageable pageable);

    // =========================================================
    // 2) Status
    // =========================================================

    List<Payment> findAllByStatus(PaymentStatus status);

    long countByStatus(PaymentStatus status);

    // =========================================================
    // 3) Méthode
    // =========================================================

    List<Payment> findAllByPaymentMethod(PaymentMethod method);

    // =========================================================
    // 4) Montant
    // =========================================================

    List<Payment> findAllByAmountGreaterThanEqual(BigDecimal minAmount);

    @Query("""
        select sum(p.amount)
        from Payment p
        where p.status = com.recyclix.backend.model.Payment$PaymentStatus.COMPLETED
    """)
    BigDecimal sumCompletedPayments();

    // =========================================================
    // 5) Dates
    // =========================================================

    List<Payment> findAllByPaymentDateBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    // =========================================================
    // 6) Update status
    // =========================================================

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Payment p
        set p.status = :status
        where p.id = :id
    """)
    int updateStatus(
            @Param("id") Long id,
            @Param("status") PaymentStatus status
    );

    // =========================================================
    // 7) Lock (transaction financière)
    // =========================================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.id = :id")
    Optional<Payment> lockById(@Param("id") Long id);

    List<Payment> findAllByAccountIdOrderByPaymentDateDesc(Long accountId);

    long countByAccountId(Long accountId);

    List<Payment> findByStatus(PaymentStatus paymentStatus);

    // Dans PaymentRepository.java
    @Query("SELECT p FROM Payment p JOIN FETCH p.account WHERE p.status = :status")
    List<Payment> findByStatusWithAccount(@Param("status") Payment.PaymentStatus status);

    @Query("SELECT p FROM Payment p JOIN FETCH p.account")
    Page<Payment> findAllWithAccount(Pageable pageable);
}