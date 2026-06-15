// service/admin/CancellationMonitoringService.java
package com.recyclix.backend.service.admin;

import com.recyclix.backend.model.Account;
import com.recyclix.backend.model.Notification;
import com.recyclix.backend.repository.AccountRepository;
import com.recyclix.backend.repository.CancellationLogRepository;
import com.recyclix.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CancellationMonitoringService {

    private final CancellationLogRepository cancellationLogRepository;
    private final AccountRepository accountRepository;
    private final AdminNotificationAndLogService adminNotificationAndLogService;
    private final NotificationRepository notificationRepository;

    // ============================================================
    // SEUILS D'AFFICHAGE (pour que le tableau admin montre dès 1 annulation)
    // ============================================================
    public static int getClientDisplayThreshold() { return 1; }
    public static int getCollectorDisplayThreshold() { return 1; }

    // ============================================================
    // SEUILS D'ALERTE (WARNING - notification admin + utilisateur)
    // ============================================================
    private static final int CLIENT_WARNING_THRESHOLD = 6;
    private static final int COLLECTOR_WARNING_THRESHOLD = 3;

    // ============================================================
    // SEUILS CRITIQUES (blocage du compte)
    // ============================================================
    private static final int CLIENT_CRITICAL_THRESHOLD = 10;
    private static final int COLLECTOR_CRITICAL_THRESHOLD = 6;

    // Période glissante (7 jours)
    private static final int DAYS_LOOKBACK = 7;

    // ============================================================
    // GETTERS POUR LES AUTRES SERVICES (AdminWebController)
    // ============================================================
    public static int getClientWarningThreshold() { return CLIENT_WARNING_THRESHOLD; }
    public static int getClientCriticalThreshold() { return CLIENT_CRITICAL_THRESHOLD; }
    public static int getCollectorWarningThreshold() { return COLLECTOR_WARNING_THRESHOLD; }
    public static int getCollectorCriticalThreshold() { return COLLECTOR_CRITICAL_THRESHOLD; }

    /**
     * Vérifie les annulations d'un compte et applique alertes/blocage si nécessaire.
     * Appelée après chaque annulation.
     */
    @Transactional
    public void checkAccountCancellations(Long accountId) {
        Account account = accountRepository.findById(accountId).orElse(null);
        if (account == null) return;

        LocalDateTime since = LocalDateTime.now().minusDays(DAYS_LOOKBACK);
        long cancelCount = cancellationLogRepository.countByAccountIdAndCancelledAtAfter(accountId, since);

        boolean isCollector = account.getRoleType() == Account.RoleType.COLLECTOR;
        int warningThreshold = isCollector ? COLLECTOR_WARNING_THRESHOLD : CLIENT_WARNING_THRESHOLD;
        int criticalThreshold = isCollector ? COLLECTOR_CRITICAL_THRESHOLD : CLIENT_CRITICAL_THRESHOLD;

        // 1. Seuil critique -> blocage
        if (cancelCount >= criticalThreshold) {
            if (account.getStatus() != Account.AccountStatus.INACTIVE) {
                account.setStatus(Account.AccountStatus.INACTIVE);
                accountRepository.save(account);
                log.warn("Compte {} bloqué pour annulations excessives ({} annulations en {} jours)",
                        account.getEmail(), cancelCount, DAYS_LOOKBACK);

                // Anomalie CRITICAL
                adminNotificationAndLogService.detectAndLogAnomaly(
                        "ANNULATIONS_EXCESSIVES",
                        "CRITICAL",
                        String.format("Compte %s bloqué : %d annulations en %d jours",
                                account.getEmail(), cancelCount, DAYS_LOOKBACK),
                        accountId,
                        "Account"
                );

                // Notification à l'utilisateur (blocage)
                sendBlockNotification(account, cancelCount);
            }
        }
        // 2. Seuil d'alerte -> warning + notification à l'utilisateur
        else if (cancelCount >= warningThreshold) {
            adminNotificationAndLogService.detectAndLogAnomaly(
                    "ANNULATIONS_ELEVEES",
                    "WARNING",
                    String.format("Compte %s : %d annulations en %d jours (seuil %d)",
                            account.getEmail(), cancelCount, DAYS_LOOKBACK, warningThreshold),
                    accountId,
                    "Account"
            );

            // 🔥 Notification d'alerte à l'utilisateur
            sendWarningNotification(account, cancelCount, warningThreshold);
        }
    }

    /**
     * Envoie une notification d'alerte à l'utilisateur (avant blocage)
     */
    private void sendWarningNotification(Account account, long cancelCount, int warningThreshold) {
        try {
            String roleLabel = account.getRoleType() == Account.RoleType.CLIENT ? "client" : "collecteur";
            int criticalLimit = account.getRoleType() == Account.RoleType.CLIENT ? 10 : 6;

            Notification notif = Notification.builder()
                    .account(account)
                    .title("⚠️ Attention : annulations excessives")
                    .message(String.format(
                            "Vous avez effectué %d annulations au cours des 7 derniers jours.\n\n" +
                                    "⚠️ Seuil d'alerte : %d annulations pour un %s.\n" +
                                    "🔴 Seuil de blocage : %d annulations.\n\n" +
                                    "Si vous continuez à annuler, votre compte sera automatiquement désactivé.\n" +
                                    "Contactez le support si vous rencontrez des problèmes.",
                            cancelCount, warningThreshold, roleLabel, criticalLimit))
                    .type(Notification.NotificationType.WARNING)
                    .isRead(false)
                    .build();
            notificationRepository.save(notif);
            log.info("Notification d'alerte envoyée à l'utilisateur {} ({} annulations)",
                    account.getEmail(), cancelCount);
        } catch (Exception e) {
            log.error("Impossible d'envoyer la notification d'alerte à l'utilisateur {}", account.getId(), e);
        }
    }

    /**
     * Envoie une notification de blocage à l'utilisateur
     */
    private void sendBlockNotification(Account account, long cancelCount) {
        try {
            String roleLabel = account.getRoleType() == Account.RoleType.CLIENT ? "client" : "collecteur";
            Notification notif = Notification.builder()
                    .account(account)
                    .title("🔒 Compte désactivé")
                    .message(String.format(
                            "Votre compte a été désactivé en raison d'annulations excessives.\n\n" +
                                    "📊 Vous avez effectué %d annulations au cours des 7 derniers jours.\n" +
                                    "🔴 Le seuil de blocage pour un %s est de %d annulations.\n\n" +
                                    "Contactez l'administrateur pour faire réactiver votre compte.",
                            cancelCount, roleLabel,
                            account.getRoleType() == Account.RoleType.CLIENT ? 10 : 6))
                    .type(Notification.NotificationType.ERROR)
                    .isRead(false)
                    .build();
            notificationRepository.save(notif);
            log.info("Notification de blocage envoyée à l'utilisateur {}", account.getEmail());
        } catch (Exception e) {
            log.error("Impossible d'envoyer la notification de blocage à l'utilisateur {}", account.getId(), e);
        }
    }

    /**
     * Tâche programmée pour vérifier tous les comptes périodiquement
     * (exécution quotidienne à 2h du matin)
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void scheduledCheckAllAccounts() {
        log.info("Exécution du job de surveillance des annulations");
        List<Account> allAccounts = accountRepository.findAll();
        for (Account account : allAccounts) {
            if (account.getRoleType() == Account.RoleType.CLIENT ||
                    account.getRoleType() == Account.RoleType.COLLECTOR) {
                checkAccountCancellations(account.getId());
            }
        }
    }

    /**
     * Réactive un compte bloqué (appelé par l'admin)
     */
    @Transactional
    public void reactivateAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Compte introuvable"));
        if (account.getStatus() == Account.AccountStatus.INACTIVE) {
            account.setStatus(Account.AccountStatus.ACTIVE);
            accountRepository.save(account);
            log.info("Compte {} réactivé par admin", account.getEmail());

            // Notification de réactivation
            Notification notif = Notification.builder()
                    .account(account)
                    .title("✅ Compte réactivé")
                    .message("Votre compte a été réactivé par l'administrateur. Vous pouvez à nouveau vous connecter. Veuillez éviter les annulations excessives à l'avenir.")
                    .type(Notification.NotificationType.SUCCESS)
                    .isRead(false)
                    .build();
            notificationRepository.save(notif);
        }
    }
}