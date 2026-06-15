// recyclix\backend\service\admin\AdminNotificationAndLogService.java
package com.recyclix.backend.service.admin;

import com.recyclix.backend.dto.admin.*;
import com.recyclix.backend.dto.notification.NotificationResponseDTO;
import com.recyclix.backend.dto.notification.NotificationSummaryDTO;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.exception.UnauthorizedException;
import com.recyclix.backend.mapper.NotificationMapper;
import com.recyclix.backend.model.*;
import com.recyclix.backend.model.Collection;
import com.recyclix.backend.model.Notification.NotificationType;
import com.recyclix.backend.repository.*;
import com.recyclix.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AdminNotificationAndLogService {

    private final NotificationRepository notificationRepo;
    private final AccountRepository accountRepo;
    private final CollectionRequestRepository requestRepo;
    private final CollectionRepository collectionRepo;
    private final FactoryDeliveryRepository deliveryRepo;
    private final FactoryValidationRepository validationRepo;
    private final SupportTicketRepository ticketRepo;
    private final AccountRepository accountRepository;
    private final NotificationMapper notificationMapper;

    // ============================================================
    // 1. STATISTIQUES DES NOTIFICATIONS
    // ============================================================
    @Transactional(readOnly = true)
    public AdminNotificationStatsDTO getNotificationStats() {
        long totalNotificationsSent = notificationRepo.count();
        long totalClients = accountRepo.countByRoleType(Account.RoleType.CLIENT);
        long totalCollectors = accountRepo.countByRoleType(Account.RoleType.COLLECTOR);
        long totalFactoryUsers = accountRepo.countByRoleType(Account.RoleType.FACTORY_USER);
        return AdminNotificationStatsDTO.builder()
                .totalNotificationsSent(totalNotificationsSent)
                .totalClients(totalClients)
                .totalCollectors(totalCollectors)
                .totalFactoryUsers(totalFactoryUsers)
                .build();
    }
    // ============================================================
    // 2. ENVOYER UNE NOTIFICATION CIBLÉE (UN OU PLUSIEURS UTILISATEURS)
    // ============================================================
    public List<NotificationResponseDTO> sendTargetedNotification(AdminNotificationSendRequestDTO request) {
        validateSendRequest(request);
        if (request.getTargetAccountIds() == null || request.getTargetAccountIds().isEmpty()) {
            throw new BadRequestException("Aucun ID utilisateur fourni.");
        }
        List<Account> targetAccounts = accountRepo.findAllById(request.getTargetAccountIds());
        if (targetAccounts.isEmpty()) {
            throw new BadRequestException("Aucun compte trouvé pour les IDs fournis.");
        }
        List<NotificationResponseDTO> sentNotifications = new ArrayList<>();
        for (Account account : targetAccounts) {
            Notification notif = Notification.builder()
                    .account(account)
                    .title(request.getTitle())
                    .message(request.getMessage())
                    .type(request.getType())
                    .isRead(false)
                    .build();
            Notification saved = notificationRepo.save(notif);
            sentNotifications.add(notificationMapper.toDto(saved));
        }
        return sentNotifications;
    }

    // ============================================================
    // 3. ENVOYER UNE ANNONCE À TOUS LES UTILISATEURS D'UN RÔLE
    // ============================================================

    public Map<String, Object> sendBroadcastAnnouncement(AdminNotificationSendRequestDTO request) {
        validateSendRequest(request);
        if (request.getTargetRole() == null || request.getTargetRole().isBlank()) {
            throw new BadRequestException("Le rôle cible est obligatoire.");
        }
        Account.RoleType roleEnum;
        try {
            roleEnum = Account.RoleType.valueOf(request.getTargetRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Rôle invalide : " + request.getTargetRole());
        }
        Notification.RoleTypeN targetRole = convertRole(roleEnum);
        if (targetRole == null) {
            throw new BadRequestException("Rôle non supporté pour les notifications.");
        }
        Notification broadcast = Notification.builder()
                .account(null)
                .targetRole(targetRole)
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType())
                .isRead(true)
                .build();
        notificationRepo.save(broadcast);
        return Map.of(
                "sentCount", "broadcast to role " + request.getTargetRole(),
                "targetRole", request.getTargetRole(),
                "title", request.getTitle()
        );
    }

    // Helper de conversion
    private Notification.RoleTypeN convertRole(Account.RoleType role) {
        if (role == null) return null;
        switch (role) {
            case CLIENT: return Notification.RoleTypeN.CITIZEN;
            case COLLECTOR: return Notification.RoleTypeN.COLLECTOR;
            default: return null;
        }
    }

    // ============================================================
    // 4. HISTORIQUE DES NOTIFICATIONS ENVOYÉES
    // ============================================================
    @Transactional(readOnly = true)
    public Page<NotificationSummaryDTO> getNotificationHistory(
            NotificationType type,
            Long accountId,
            int page,
            int size
    ) {
        List<Notification> notifications = notificationRepo.findAll();
        List<Notification> filtered = notifications.stream()
                .filter(n -> type == null || n.getType() == type)
                .filter(n -> accountId == null || (n.getAccount() != null && n.getAccount().getId().equals(accountId)))
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                .toList();
        int start = page * size;
        int end = Math.min(start + size, filtered.size());
        if (start >= filtered.size()) {
            return new PageImpl<>(List.of(), PageRequest.of(page, size), filtered.size());
        }
        List<NotificationSummaryDTO> dtos = filtered.subList(start, end).stream()
                .map(notificationMapper::toSummaryDto)
                .toList();
        return new PageImpl<>(dtos, PageRequest.of(page, size), filtered.size());
    }

    // ============================================================
    // 5. DÉTECTER LES ANOMALIES (LOGS DE SÉCURITÉ SIMPLIFIÉS)
    // ============================================================
    @Transactional(readOnly = true)
    public AdminAnomalyStatsDTO getAnomalyStats() {

        List<AdminAnomalyDTO> anomalies = detectAllAnomalies();

        long totalAnomalies = anomalies.size();
        long criticalAnomalies = anomalies.stream()
                .filter(a -> "CRITICAL".equals(a.getSeverity()))
                .count();
        long warningAnomalies = anomalies.stream()
                .filter(a -> "WARNING".equals(a.getSeverity()))
                .count();
        long infoAnomalies = anomalies.stream()
                .filter(a -> "INFO".equals(a.getSeverity()))
                .count();

        return AdminAnomalyStatsDTO.builder()
                .totalAnomalies(totalAnomalies)
                .criticalAnomalies(criticalAnomalies)
                .warningAnomalies(warningAnomalies)
                .infoAnomalies(infoAnomalies)
                .build();
    }

    // ============================================================
    // 6. LISTE DES ANOMALIES DÉTECTÉES
    // ============================================================
    @Transactional(readOnly = true)
    public Page<AdminAnomalyDTO> getAnomalies(String severity, int page, int size) {

        List<AdminAnomalyDTO> anomalies = detectAllAnomalies();

        List<AdminAnomalyDTO> filtered = anomalies.stream()
                .filter(a -> severity == null || a.getSeverity().equalsIgnoreCase(severity))
                .sorted(Comparator.comparing(AdminAnomalyDTO::getDetectedAt).reversed())
                .toList();

        int start = page * size;
        int end = Math.min(start + size, filtered.size());

        if (start >= filtered.size()) {
            return new PageImpl<>(List.of(), PageRequest.of(page, size), filtered.size());
        }

        return new PageImpl<>(filtered.subList(start, end), PageRequest.of(page, size), filtered.size());
    }

    // ============================================================
    // 7. LOGS DES ACTIONS SENSIBLES
    // ============================================================
    @Transactional(readOnly = true)
    public List<AdminAnomalyDTO> getSensitiveActions() {

        List<AdminAnomalyDTO> sensitiveActions = new ArrayList<>();

        // 1. Validations avec ajustement de poids
        List<FactoryValidation> adjustedValidations = validationRepo.findAll().stream()
                .filter(v -> v.getAdjustmentNote() != null && !v.getAdjustmentNote().isBlank())
                .toList();

        for (FactoryValidation validation : adjustedValidations) {
            sensitiveActions.add(AdminAnomalyDTO.builder()
                    .anomalyType("VALIDATION_AJUSTÉE")
                    .severity("WARNING")
                    .description("Validation avec ajustement : " + validation.getAdjustmentNote())
                    .relatedEntityId(validation.getId())
                    .relatedEntityType("FactoryValidation")
                    .detectedAt(validation.getValidatedAt())
                    .build());
        }

        // 2. Livraisons rejetées par l'usine
        List<FactoryDelivery> refusedDeliveries = deliveryRepo.findAll().stream()
                .filter(d -> d.getStatus() == FactoryDelivery.DeliveryStatus.REFUSED)
                .toList();

        for (FactoryDelivery delivery : refusedDeliveries) {
            sensitiveActions.add(AdminAnomalyDTO.builder()
                    .anomalyType("LIVRAISON_REJETÉE")
                    .severity("CRITICAL")
                    .description("Livraison rejetée par l'usine")
                    .relatedEntityId(delivery.getId())
                    .relatedEntityType("FactoryDelivery")
                    .detectedAt(delivery.getCreatedAt())
                    .build());
        }

        // 3. Demandes annulées (potentiellement litigieuses)
        List<CollectionRequest> cancelledRequests = requestRepo.findAll().stream()
                .filter(r -> r.getStatus() == CollectionRequest.Status.CANCELLED)
                .toList();

        for (CollectionRequest request : cancelledRequests) {
            sensitiveActions.add(AdminAnomalyDTO.builder()
                    .anomalyType("DEMANDE_ANNULÉE")
                    .severity("WARNING")
                    .description("Demande de collecte annulée")
                    .relatedEntityId(request.getId())
                    .relatedEntityType("CollectionRequest")
                    .detectedAt(request.getUpdatedAt())
                    .build());
        }

        // 4. Tickets support non résolus depuis plus de 7 jours
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        List<SupportTicket> oldOpenTickets = ticketRepo.findAll().stream()
                .filter(t -> t.getStatus() == SupportTicket.Status.OPEN
                        || t.getStatus() == SupportTicket.Status.IN_PROGRESS)
                .filter(t -> t.getCreatedAt().isBefore(sevenDaysAgo))
                .toList();

        for (SupportTicket ticket : oldOpenTickets) {
            sensitiveActions.add(AdminAnomalyDTO.builder()
                    .anomalyType("TICKET_NON_RÉSOLU")
                    .severity("WARNING")
                    .description("Ticket ouvert depuis plus de 7 jours : " + ticket.getSubject())
                    .relatedEntityId(ticket.getId())
                    .relatedEntityType("SupportTicket")
                    .detectedAt(LocalDateTime.now())
                    .build());
        }

        // 5. Collectes avec poids réel très différent du poids estimé
        List<Collection> allCollections = collectionRepo.findAll();

        for (Collection collection : allCollections) {
            if (collection.getRequest() != null
                    && collection.getRequest().getEstimatedQuantity() != null
                    && collection.getRealQuantity() != null) {

                BigDecimal estimated = collection.getRequest().getEstimatedQuantity();
                BigDecimal real = collection.getRealQuantity();

                if (estimated.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal difference = estimated.subtract(real).abs()
                            .divide(estimated, 2, java.math.RoundingMode.HALF_UP);

                    if (difference.compareTo(new BigDecimal("0.5")) > 0) {
                        sensitiveActions.add(AdminAnomalyDTO.builder()
                                .anomalyType("ÉCART_POIDS")
                                .severity("INFO")
                                .description("Écart de poids > 50% : Estimé=" + estimated + "kg, Réel=" + real + "kg")
                                .relatedEntityId(collection.getId())
                                .relatedEntityType("Collection")
                                .detectedAt(collection.getCollectedAt())
                                .build());
                    }
                }
            }
        }

        return sensitiveActions.stream()
                .sorted(Comparator.comparing(AdminAnomalyDTO::getDetectedAt).reversed())
                .toList();
    }

    // ============================================================
    // HELPER PRIVÉ — DÉTECTION COMPLÈTE DES ANOMALIES
    // ============================================================
    private List<AdminAnomalyDTO> detectAllAnomalies() {

        List<AdminAnomalyDTO> anomalies = new ArrayList<>();

        // ---- ANOMALIES CRITIQUES ----

        // 1. Livraisons rejetées
        List<FactoryDelivery> refusedDeliveries = deliveryRepo.findAll().stream()
                .filter(d -> d.getStatus() == FactoryDelivery.DeliveryStatus.REFUSED)
                .toList();

        for (FactoryDelivery delivery : refusedDeliveries) {
            anomalies.add(AdminAnomalyDTO.builder()
                    .anomalyType("LIVRAISON_REJETÉE")
                    .severity("CRITICAL")
                    .description("Livraison rejetée par l'usine. Vérifier la raison du rejet.")
                    .relatedEntityId(delivery.getId())
                    .relatedEntityType("FactoryDelivery")
                    .detectedAt(delivery.getCreatedAt())
                    .build());
        }

        // ---- ANOMALIES WARNING ----

        // 2. Demandes annulées
        List<CollectionRequest> cancelledRequests = requestRepo.findAll().stream()
                .filter(r -> r.getStatus() == CollectionRequest.Status.CANCELLED)
                .toList();

        for (CollectionRequest request : cancelledRequests) {
            anomalies.add(AdminAnomalyDTO.builder()
                    .anomalyType("DEMANDE_ANNULÉE")
                    .severity("WARNING")
                    .description("Demande de collecte annulée. Client : "
                            + (request.getClient() != null
                            ? request.getClient().getFirstName() + " " + request.getClient().getLastName()
                            : "Inconnu"))
                    .relatedEntityId(request.getId())
                    .relatedEntityType("CollectionRequest")
                    .detectedAt(request.getUpdatedAt() != null ? request.getUpdatedAt() : request.getCreatedAt())
                    .build());
        }

        // 3. Tickets non résolus > 7 jours
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        List<SupportTicket> oldOpenTickets = ticketRepo.findAll().stream()
                .filter(t -> t.getStatus() == SupportTicket.Status.OPEN
                        || t.getStatus() == SupportTicket.Status.IN_PROGRESS)
                .filter(t -> t.getCreatedAt().isBefore(sevenDaysAgo))
                .toList();

        for (SupportTicket ticket : oldOpenTickets) {
            anomalies.add(AdminAnomalyDTO.builder()
                    .anomalyType("TICKET_NON_RÉSOLU")
                    .severity("WARNING")
                    .description("Ticket non résolu depuis plus de 7 jours : " + ticket.getSubject())
                    .relatedEntityId(ticket.getId())
                    .relatedEntityType("SupportTicket")
                    .detectedAt(LocalDateTime.now())
                    .build());
        }

        // 4. Validations avec ajustement de poids
        List<FactoryValidation> adjustedValidations = validationRepo.findAll().stream()
                .filter(v -> v.getAdjustmentNote() != null && !v.getAdjustmentNote().isBlank())
                .toList();

        for (FactoryValidation validation : adjustedValidations) {
            anomalies.add(AdminAnomalyDTO.builder()
                    .anomalyType("VALIDATION_AJUSTÉE")
                    .severity("WARNING")
                    .description("Validation avec ajustement : " + validation.getAdjustmentNote())
                    .relatedEntityId(validation.getId())
                    .relatedEntityType("FactoryValidation")
                    .detectedAt(validation.getValidatedAt())
                    .build());
        }

        // ---- ANOMALIES INFO ----

        // 5. Écarts de poids > 50%
        List<Collection> allCollections = collectionRepo.findAll();

        for (Collection collection : allCollections) {
            if (collection.getRequest() != null
                    && collection.getRequest().getEstimatedQuantity() != null
                    && collection.getRealQuantity() != null) {

                BigDecimal estimated = collection.getRequest().getEstimatedQuantity();
                BigDecimal real = collection.getRealQuantity();

                if (estimated.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal difference = estimated.subtract(real).abs()
                            .divide(estimated, 2, java.math.RoundingMode.HALF_UP);

                    if (difference.compareTo(new BigDecimal("0.5")) > 0) {
                        anomalies.add(AdminAnomalyDTO.builder()
                                .anomalyType("ÉCART_POIDS")
                                .severity("INFO")
                                .description("Écart de poids > 50% : Estimé="
                                        + estimated.setScale(2, java.math.RoundingMode.HALF_UP)
                                        + "kg, Réel="
                                        + real.setScale(2, java.math.RoundingMode.HALF_UP) + "kg")
                                .relatedEntityId(collection.getId())
                                .relatedEntityType("Collection")
                                .detectedAt(collection.getCollectedAt())
                                .build());
                    }
                }
            }
        }

        return anomalies.stream()
                .sorted(Comparator.comparing(AdminAnomalyDTO::getDetectedAt).reversed())
                .toList();
    }

    // ============================================================
    // HELPER PRIVÉ — VALIDATION
    // ============================================================
    private void validateSendRequest(AdminNotificationSendRequestDTO request) {
        if (request == null) {
            throw new BadRequestException("Les données de la notification sont obligatoires.");
        }

        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new BadRequestException("Le titre est obligatoire.");
        }

        if (request.getMessage() == null || request.getMessage().isBlank()) {
            throw new BadRequestException("Le message est obligatoire.");
        }

        if (request.getType() == null) {
            throw new BadRequestException("Le type de notification est obligatoire.");
        }
    }


    /**
     * Récupère l'historique des notifications filtré par rôle cible.
     * Utile pour savoir quelles annonces ont été envoyées à quel rôle.
     */
    @Transactional(readOnly = true)
    public Page<NotificationSummaryDTO> getNotificationHistoryByRole(
            Account.RoleType targetRole,
            int page,
            int size
    ) {
        List<Account> roleAccounts = accountRepo.findAllByRoleType(targetRole);
        List<Long> accountIds = roleAccounts.stream().map(Account::getId).toList();
        List<Notification> allNotifications = new ArrayList<>();
        for (Long accountId : accountIds) {
            allNotifications.addAll(notificationRepo.findByAccountIdOrderByCreatedAtDesc(accountId));
        }
        List<Notification> sorted = allNotifications.stream()
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                .toList();
        int start = page * size;
        int end = Math.min(start + size, sorted.size());
        if (start >= sorted.size()) {
            return new PageImpl<>(List.of(), PageRequest.of(page, size), sorted.size());
        }
        List<NotificationSummaryDTO> dtos = sorted.subList(start, end).stream()
                .map(notificationMapper::toSummaryDto)
                .toList();
        return new PageImpl<>(dtos, PageRequest.of(page, size), sorted.size());
    }

    // Dans AdminNotificationAndLogService.java

    /**
     * Enregistre une anomalie dans le système (pour monitoring)
     */
    public void detectAndLogAnomaly(String anomalyType, String severity, String description,
                                    Long relatedEntityId, String relatedEntityType) {
        // Option 1 : stocker dans une table d'anomalies (à créer)
        // Option 2 : utiliser la méthode existante getAnomalies() qui scanne dynamiquement
        // Pour l'instant, on log simplement et on pourrait ajouter à une table si besoin.
        log.warn("ANOMALIE [{}] [{}] : {} (entité: {}#{})",
                severity, anomalyType, description, relatedEntityType, relatedEntityId);

        // Si vous avez une entité AnomalyLog, persistez-la ici.
        // Sinon, les alertes seront remontées via getAnomalies() qui scanne les logs.
        // Pour que l'admin voie l'alerte immédiatement, on peut stocker dans une table temporaire.
    }
}