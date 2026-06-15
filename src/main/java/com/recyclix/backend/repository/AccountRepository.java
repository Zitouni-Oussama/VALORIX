package com.recyclix.backend.repository;

import com.recyclix.backend.model.Account;
import com.recyclix.backend.model.Account.AccountStatus;
import com.recyclix.backend.model.Account.RoleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {

    // =========================================================
    // 1) Uniques + lookup principal
    // email est unique (unique = true) :contentReference[oaicite:1]{index=1}
    // =========================================================

    Optional<Account> findByEmail(String email);

    boolean existsByEmail(String email);

    void deleteByEmail(String email); // (hard delete) -> à utiliser seulement si tu veux supprimer réellement

    // phone n'est pas unique dans ton modèle, mais on peut quand même chercher
    Optional<Account> findFirstByPhone(String phone);

    List<Account> findAllByPhone(String phone);

    // =========================================================
    // 2) Status / RoleType (Enums) :contentReference[oaicite:2]{index=2}
    // =========================================================

    List<Account> findAllByStatus(AccountStatus status);

    Page<Account> findAllByStatus(AccountStatus status, Pageable pageable);

    List<Account> findAllByRoleType(RoleType roleType);

    Page<Account> findAllByRoleType(RoleType roleType, Pageable pageable);

    List<Account> findAllByRoleTypeAndStatus(RoleType roleType, AccountStatus status);

    Page<Account> findAllByRoleTypeAndStatus(RoleType roleType, AccountStatus status, Pageable pageable);

    long countByStatus(AccountStatus status);

    long countByRoleType(RoleType roleType);

    long countByRoleTypeAndStatus(RoleType roleType, AccountStatus status);

    // =========================================================
    // 3) Recherche texte (email) + filtres dates :contentReference[oaicite:3]{index=3}
    // =========================================================

    Page<Account> findByEmailContainingIgnoreCase(String q, Pageable pageable);

    List<Account> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    Page<Account> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Account> findAllByUpdatedAtAfter(LocalDateTime since);

    // =========================================================
    // 4) Relations : fetch léger (EntityGraph) pour éviter lazy + N+1
    // Relations: client/collector/factoryUser/wallet :contentReference[oaicite:4]{index=4}
    // =========================================================

    @EntityGraph(attributePaths = {"client"})
    Optional<Account> findWithClientById(Long id);

    @EntityGraph(attributePaths = {"collector"})
    Optional<Account> findWithCollectorById(Long id);

    @EntityGraph(attributePaths = {"factoryUser"})
    Optional<Account> findWithFactoryUserById(Long id);

    @EntityGraph(attributePaths = {"wallet"})
    Optional<Account> findWithWalletById(Long id);

    @EntityGraph(attributePaths = {"client", "collector", "factoryUser", "wallet"})
    Optional<Account> findWithProfileById(Long id);

    // --- Lookup par relations (ids) (ex: récupérer Account via Client/Collector/FactoryUser/Wallet)
    @Query("select a from Account a join a.client c where c.id = :clientId")
    Optional<Account> findByClientId(@Param("clientId") Long clientId);

    @Query("select a from Account a join a.collector c where c.id = :collectorId")
    Optional<Account> findByCollectorId(@Param("collectorId") Long collectorId);

    @Query("select a from Account a join a.factoryUser f where f.id = :factoryUserId")
    Optional<Account> findByFactoryUserId(@Param("factoryUserId") Long factoryUserId);

    @Query("select a from Account a join a.wallet w where w.id = :walletId")
    Optional<Account> findByWalletId(@Param("walletId") Long walletId);

    // =========================================================
    // 5) Soft-delete + updates ciblés (status, image, password)
    // status existe dans ton modèle (ACTIVE/INACTIVE/DELETED) :contentReference[oaicite:5]{index=5}
    // =========================================================

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Account a set a.status = :status where a.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") AccountStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Account a set a.profileImageUrl = :url where a.id = :id")
    int updateProfileImageUrl(@Param("id") Long id, @Param("url") String url);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Account a set a.passwordHash = :hash where a.id = :id")
    int updatePasswordHash(@Param("id") Long id, @Param("hash") String hash);

    // Soft delete "classique" : passer à DELETED
    default void softDeleteById(Long id) {
        updateStatus(id, AccountStatus.DELETED);
    }

    // =========================================================
    // 6) Locking (utile si tu veux éviter conflits de maj concurrentes)
    // =========================================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :id")
    Optional<Account> lockById(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.email = :email")
    Optional<Account> lockByEmail(@Param("email") String email);

    // =========================================================
    // 7) Helpers “actifs”
    // =========================================================

    @Query("select a from Account a where a.email = :email and a.status <> com.recyclix.backend.model.Account$AccountStatus.DELETED")
    Optional<Account> findActiveOrInactiveByEmailExcludeDeleted(@Param("email") String email);

    @Query("select a from Account a where a.status = com.recyclix.backend.model.Account$AccountStatus.ACTIVE")
    Page<Account> findAllActive(Pageable pageable);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // Pour les nouveaux inscrits par jour de la semaine (optionnel pour graphique)
    @Query("SELECT DATE(a.createdAt), COUNT(a) FROM Account a WHERE a.createdAt >= :start GROUP BY DATE(a.createdAt)")
    List<Object[]> countNewUsersByDay(@Param("start") LocalDateTime start);

    @EntityGraph(attributePaths = {"client", "collector", "factoryUser"})
    Page<Account> findAll(Specification<Account> spec, Pageable pageable);


    List<Account> findByRoleType(Account.RoleType roleType);
}