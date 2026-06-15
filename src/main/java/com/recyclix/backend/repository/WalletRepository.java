package com.recyclix.backend.repository;

import com.recyclix.backend.model.Transaction;
import com.recyclix.backend.model.Wallet;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long>, JpaSpecificationExecutor<Wallet> {
    
    Optional<Wallet> findByAccountId(Long accountId);
    boolean existsByAccountId(Long accountId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Transaction> findAllByAccountIdOrderByCreatedAtDesc(Long accountId);

    long countByAccountId(Long accountId);

    @Query("SELECT SUM(w.balancePoints) FROM Wallet w")
    Long sumAllPoints();


    @Query("""
    SELECT COALESCE(SUM(w.balancePoints), 0)
    FROM Wallet w
    WHERE w.account.roleType = com.recyclix.backend.model.Account.RoleType.CLIENT 
    """)
    Long sumClientPoints();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT w
    FROM Wallet w
    WHERE w.account.id = :accountId
    """)
    Optional<Wallet> lockByAccountId(@Param("accountId") Long accountId);
}