package com.recyclix.backend.service.admin;

import com.recyclix.backend.dto.account.AccountResponseDTO;
import com.recyclix.backend.dto.account.AccountSummaryDTO;
import com.recyclix.backend.dto.account.AccountUpdateDTO;
import com.recyclix.backend.dto.admin.AdminUserDetailsDTO;
import com.recyclix.backend.dto.factory_user.FactoryUserResponseDTO;
import com.recyclix.backend.dto.factory_user.FactoryUserSummaryDTO;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.exception.UnauthorizedException;
import com.recyclix.backend.mapper.AccountMapper;
import com.recyclix.backend.mapper.FactoryUserMapper;
import com.recyclix.backend.model.Account;
import com.recyclix.backend.model.Client;
import com.recyclix.backend.model.Collector;
import com.recyclix.backend.model.FactoryUser;
import com.recyclix.backend.repository.AccountRepository;
import com.recyclix.backend.repository.FactoryUserRepository;
import com.recyclix.backend.util.SecurityUtils;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.recyclix.backend.exception.GlobalExceptionHandler.log;
import java.math.BigDecimal;


@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final PasswordEncoder passwordEncoder;
    private final FactoryUserRepository factoryUserRepository;
    private final FactoryUserMapper factoryUserMapper;

    @Transactional(readOnly = true)
    public Page<AccountSummaryDTO> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return accountRepository.findAll(pageable)
                .map(accountMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public Page<AccountSummaryDTO> searchUsers(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        if (keyword == null || keyword.isBlank()) {
            return getAllUsers(page, size);
        }

        return accountRepository.findByEmailContainingIgnoreCase(keyword.trim(), pageable)
                .map(accountMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public Page<AccountSummaryDTO> getUsersByRole(Account.RoleType roleType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return accountRepository.findAllByRoleType(roleType, pageable)
                .map(accountMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public Page<AccountSummaryDTO> getUsersByStatus(Account.AccountStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return accountRepository.findAllByStatus(status, pageable)
                .map(accountMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public AccountResponseDTO getUserById(Long id) {
        Account account = accountRepository.findWithProfileById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable avec id : " + id));

        return accountMapper.toDto(account);
    }

    @Transactional
    public AccountResponseDTO activateUser(Long id) {
        Account account = getAccountOrThrow(id);
        account.setStatus(Account.AccountStatus.ACTIVE);

        return accountMapper.toDto(accountRepository.save(account));
    }

    @Transactional
    public AccountResponseDTO deactivateUser(Long id) {
        Account account = getAccountOrThrow(id);
        account.setStatus(Account.AccountStatus.INACTIVE);

        return accountMapper.toDto(accountRepository.save(account));
    }

    @Transactional
    public AccountResponseDTO blockUser(Long id) {
        Account account = getAccountOrThrow(id);
        account.setStatus(Account.AccountStatus.INACTIVE);

        return accountMapper.toDto(accountRepository.save(account));
    }

    @Transactional
    public void softDeleteUser(Long id) {
        Account account = getAccountOrThrow(id);
        account.setStatus(Account.AccountStatus.DELETED);
        accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public UserStatsResponse getUserStats() {
        return UserStatsResponse.builder()
                .totalUsers(accountRepository.count())
                .activeUsers(accountRepository.countByStatus(Account.AccountStatus.ACTIVE))
                .inactiveUsers(accountRepository.countByStatus(Account.AccountStatus.INACTIVE))
                .deletedUsers(accountRepository.countByStatus(Account.AccountStatus.DELETED))
                .clients(accountRepository.countByRoleType(Account.RoleType.CLIENT))
                .collectors(accountRepository.countByRoleType(Account.RoleType.COLLECTOR))
                .factoryUsers(accountRepository.countByRoleType(Account.RoleType.FACTORY_USER))
                .build();
    }

    private Account getAccountOrThrow(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable avec id : " + id));
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserStatsResponse {
        private Long totalUsers;
        private Long activeUsers;
        private Long inactiveUsers;
        private Long deletedUsers;
        private Long clients;
        private Long collectors;
        private Long factoryUsers;
    }

    public Long countNewUsersToday() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return accountRepository.countByCreatedAtBetween(start, end);
    }

    public Long countNewUsersThisWeek() {
        LocalDateTime start = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime end = LocalDate.now().plusDays(1).atStartOfDay();
        return accountRepository.countByCreatedAtBetween(start, end);
    }

    // Pour le graphique : inscriptions par jour de la semaine en cours
    public List<Long> getNewUsersPerDayThisWeek() {
        LocalDateTime start = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
        List<Long> counts = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDateTime dayStart = start.plusDays(i);
            LocalDateTime dayEnd = dayStart.plusDays(1);
            Long count = accountRepository.countByCreatedAtBetween(dayStart, dayEnd);
            counts.add(count);
        }
        return counts;
    }

    public Page<AccountSummaryDTO> searchUsersWithFilters(String keyword, Account.RoleType role, Account.AccountStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // Si aucun filtre n'est actif, retourner tous les utilisateurs
        if ((keyword == null || keyword.isBlank()) && role == null && status == null) {
            return getAllUsers(page, size);
        }

        // Initialisation avec une condition toujours vraie (conjunction)
        Specification<Account> spec = (root, query, cb) -> cb.conjunction();

        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("email")), "%" + keyword.toLowerCase() + "%"));
        }
        if (role != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("roleType"), role));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        return accountRepository.findAll(spec, pageable)
                .map(accountMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public Page<Account> searchUsersWithFiltersEntity(String keyword, Account.RoleType role, Account.AccountStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        if ((keyword == null || keyword.isBlank()) && role == null && status == null) {
            return accountRepository.findAll(pageable);
        }

        Specification<Account> spec = (root, query, cb) -> cb.conjunction();

        if (keyword != null && !keyword.isBlank()) {
            String searchTerm = "%" + keyword.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> {
                // Jointures gauches pour accéder aux noms des différents profils
                var clientJoin = root.join("client", jakarta.persistence.criteria.JoinType.LEFT);
                var collectorJoin = root.join("collector", jakarta.persistence.criteria.JoinType.LEFT);
                var factoryUserJoin = root.join("factoryUser", jakarta.persistence.criteria.JoinType.LEFT);

                return cb.or(
                        cb.like(cb.lower(root.get("email")), searchTerm),
                        cb.like(cb.lower(clientJoin.get("firstName")), searchTerm),
                        cb.like(cb.lower(clientJoin.get("lastName")), searchTerm),
                        cb.like(cb.lower(collectorJoin.get("firstName")), searchTerm),
                        cb.like(cb.lower(collectorJoin.get("lastName")), searchTerm),
                        cb.like(cb.lower(factoryUserJoin.get("firstName")), searchTerm),
                        cb.like(cb.lower(factoryUserJoin.get("lastName")), searchTerm)
                );
            });
        }
        if (role != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("roleType"), role));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        return accountRepository.findAll(spec, pageable);
    }

    @Transactional
    public void resetUserPassword(Long userId, String newPassword) {
        System.out.println("=== resetUserPassword CALLED ===");
        System.out.println("userId: " + userId);
        System.out.println("newPassword length: " + (newPassword != null ? newPassword.length() : 0));

        try {
            // 1. Vérifier que l'utilisateur connecté est un ADMIN
            Long currentAccountId = SecurityUtils.getAccountId()
                    .orElseThrow(() -> {
                        log.error("Aucun accountId trouvé dans le contexte de sécurité");
                        return new UnauthorizedException("Utilisateur non authentifié.");
                    });
            log.info("AccountId de l'utilisateur connecté : {}", currentAccountId);

            FactoryUser currentFactoryUser = factoryUserRepository.findByAccountId(currentAccountId)
                    .orElseThrow(() -> {
                        log.error("Aucun FactoryUser trouvé pour accountId {}", currentAccountId);
                        return new UnauthorizedException("Accès réservé au personnel usine.");
                    });
            log.info("FactoryUser trouvé : id={}, position={}", currentFactoryUser.getId(), currentFactoryUser.getPosition());

            if (currentFactoryUser.getPosition() != FactoryUser.FactoryPosition.ADMIN) {
                log.warn("L'utilisateur {} n'a pas le rôle ADMIN (position={})", currentAccountId, currentFactoryUser.getPosition());
                throw new UnauthorizedException("Seul un administrateur peut réinitialiser les mots de passe.");
            }
            log.info("Vérification ADMIN : OK");

            // 2. Valider le nouveau mot de passe
            if (newPassword == null || newPassword.length() < 8) {
                log.warn("Mot de passe invalide : null ou trop court ({})", newPassword != null ? newPassword.length() : "null");
                throw new BadRequestException("Le nouveau mot de passe doit comporter au moins 8 caractères.");
            }
            log.info("Validation mot de passe : OK");

            // 3. Récupérer le compte cible
            Account targetAccount = accountRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.error("Compte cible introuvable : ID {}", userId);
                        return new ResourceNotFoundException("Compte utilisateur introuvable.");
                    });
            log.info("Compte cible trouvé : id={}, email={}, ancien hash={}", targetAccount.getId(), targetAccount.getEmail(), targetAccount.getPasswordHash());

            // 4. Mettre à jour le mot de passe
            String encodedPassword = passwordEncoder.encode(newPassword);
            log.info("Nouveau hash généré : {}", encodedPassword);
            System.out.println("=== SAVING PASSWORD ===");
            targetAccount.setPasswordHash(encodedPassword);
            accountRepository.save(targetAccount);
            System.out.println("=== PASSWORD UPDATED ===");
            log.info("Mot de passe mis à jour et sauvegardé pour l'utilisateur ID {}", userId);

        } catch (Exception e) {
            log.error("Exception lors de la réinitialisation : {}", e.getMessage(), e);
            throw e;
        }

        log.info("=== FIN resetUserPassword (succès) ===");
    }

    // Dans com.recyclix.backend.service.admin.AdminUserService

    @Transactional(readOnly = true)
    public AdminUserDetailsDTO getUserFullDetails(Long id) {
        Account account = accountRepository.findWithProfileById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable avec id : " + id));

        var builder = AdminUserDetailsDTO.builder()
                .id(account.getId())
                .email(account.getEmail())
                .phone(account.getPhone())
                .roleType(account.getRoleType())
                .status(account.getStatus())
                .profileImageUrl(account.getProfileImageUrl())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .transactionsCount(account.getTransactions() != null ? account.getTransactions().size() : 0)
                .pointMovementsCount(account.getPointMovements() != null ? account.getPointMovements().size() : 0)
                .paymentsCount(account.getPayments() != null ? account.getPayments().size() : 0)
                .notificationsCount(account.getNotifications() != null ? account.getNotifications().size() : 0)
                .userChallengesCount(account.getUserChallenges() != null ? account.getUserChallenges().size() : 0)
                .supportTicketsCount(account.getSupportTickets() != null ? account.getSupportTickets().size() : 0);

        if (account.getClient() != null) {
            var client = account.getClient();
            builder.firstName(client.getFirstName())
                    .lastName(client.getLastName())
                    .fullName(client.getFirstName() + " " + client.getLastName())
                    .address(client.getAddress())
                    .latitude(client.getLatitude())
                    .longitude(client.getLongitude())
                    .totalPoints(client.getTotalPoints());
            builder.leaderboardsCount(client.getLeaderboards() != null ? client.getLeaderboards().size() : 0);
        } else if (account.getCollector() != null) {
            var collector = account.getCollector();
            builder.firstName(collector.getFirstName())
                    .lastName(collector.getLastName())
                    .fullName(collector.getFirstName() + " " + collector.getLastName())
                    .nationalIdNumber(collector.getNationalIdNumber())
                    .isVerified(collector.getIsVerified())
                    .averageRating(collector.getAverageRating());
        } else if (account.getFactoryUser() != null) {
            var factoryUser = account.getFactoryUser();
            builder.firstName(factoryUser.getFirstName())
                    .factoryUserId(factoryUser.getId())
                    .lastName(factoryUser.getLastName())
                    .fullName(factoryUser.getFirstName() + " " + factoryUser.getLastName())
                    .employeeNumber(factoryUser.getEmployeeNumber())
                    .position(factoryUser.getPosition() != null ? factoryUser.getPosition().name() : null)
                    .isHeadAccountant(factoryUser.getIsHeadAccountant());
        }

        return builder.build();
    }

    //. ==================== RÉCUPÉRATION PAR RÔLE SPÉCIFIQUE ====================
    @Transactional(readOnly = true)
    public Page<AccountSummaryDTO> getClients(int page, int size, String keyword, Account.AccountStatus status, Integer minPoints, Integer maxPoints) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Specification<Account> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("roleType"), Account.RoleType.CLIENT));
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.toLowerCase() + "%";
                Join<Account, Client> clientJoin = root.join("client", JoinType.LEFT);
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("email")), like),
                        cb.like(cb.lower(clientJoin.get("firstName")), like),
                        cb.like(cb.lower(clientJoin.get("lastName")), like)
                ));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (minPoints != null || maxPoints != null) {
                Join<Account, Client> clientJoin = root.join("client", JoinType.LEFT);
                if (minPoints != null) {
                    predicates.add(cb.greaterThanOrEqualTo(clientJoin.get("totalPoints"), minPoints));
                }
                if (maxPoints != null) {
                    predicates.add(cb.lessThanOrEqualTo(clientJoin.get("totalPoints"), maxPoints));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return accountRepository.findAll(spec, pageable).map(accountMapper::toSummaryDto);
    }


    // Dans AdminUserService
    @Transactional
    public AccountResponseDTO updateUser(Long id, AccountUpdateDTO dto) {
        Account account = getAccountOrThrow(id);
        accountMapper.updateEntityFromDto(dto, account);
        accountRepository.save(account);
        return accountMapper.toDto(account);
    }

    @Transactional(readOnly = true)
    public Page<AccountSummaryDTO> getFactoryUsersByPosition(int page, int size, String keyword, Account.AccountStatus status, FactoryUser.FactoryPosition position, Long centerId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Specification<Account> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("roleType"), Account.RoleType.FACTORY_USER));
            Join<Account, FactoryUser> fuJoin = root.join("factoryUser", JoinType.LEFT);
            predicates.add(cb.equal(fuJoin.get("position"), position));
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("email")), like),
                        cb.like(cb.lower(fuJoin.get("firstName")), like),
                        cb.like(cb.lower(fuJoin.get("lastName")), like),
                        cb.like(cb.lower(fuJoin.get("employeeNumber")), like)
                ));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (centerId != null) {
                predicates.add(cb.equal(fuJoin.get("recyclingCenter").get("id"), centerId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return accountRepository.findAll(spec, pageable).map(accountMapper::toSummaryDto);
    }





    @Transactional(readOnly = true)
    public Page<AccountSummaryDTO> getCollectors(
            int page,
            int size,
            String keyword,
            Account.AccountStatus status,
            Boolean verified,
            BigDecimal minRating) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Specification<Account> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Rôle = COLLECTOR
            predicates.add(cb.equal(root.get("roleType"), Account.RoleType.COLLECTOR));

            // 2. Jointure avec Collector
            Join<Account, Collector> colJoin = root.join("collector", JoinType.LEFT);

            // 3. Filtre par mot-clé (email, prénom, nom)
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("email")), like),
                        cb.like(cb.lower(colJoin.get("firstName")), like),
                        cb.like(cb.lower(colJoin.get("lastName")), like)
                ));
            }

            // 4. Filtre par statut du compte
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // 5. Filtre par vérification
            if (verified != null) {
                predicates.add(cb.equal(colJoin.get("isVerified"), verified));
            }

            // 6. Filtre par note moyenne minimale
            if (minRating != null) {
                predicates.add(cb.greaterThanOrEqualTo(colJoin.get("averageRating"), minRating));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return accountRepository.findAll(spec, pageable)
                .map(accountMapper::toSummaryDto);
    }


}