package com.recyclix.backend.service.client;

import com.recyclix.backend.dto.faq_entry.FaqEntrySummaryDTO;
import com.recyclix.backend.dto.notification.NotificationResponseDTO;
import com.recyclix.backend.dto.notification.NotificationSummaryDTO;
import com.recyclix.backend.dto.support_ticket.SupportTicketRequestDTO;
import com.recyclix.backend.dto.support_ticket.SupportTicketResponseDTO;
import com.recyclix.backend.dto.support_ticket.SupportTicketSummaryDTO;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.exception.UnauthorizedException;
import com.recyclix.backend.mapper.FaqEntryMapper;
import com.recyclix.backend.mapper.NotificationMapper;
import com.recyclix.backend.mapper.SupportTicketMapper;
import com.recyclix.backend.model.Account;
import com.recyclix.backend.model.FaqEntry;
import com.recyclix.backend.model.Notification;
import com.recyclix.backend.model.SupportTicket;
import com.recyclix.backend.repository.AccountRepository;
import com.recyclix.backend.repository.FaqEntryRepository;
import com.recyclix.backend.repository.NotificationRepository;
import com.recyclix.backend.repository.SupportTicketRepository;
import com.recyclix.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientSupportService {

    private final AccountRepository accountRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final NotificationRepository notificationRepository;
    private final FaqEntryRepository faqEntryRepository;

    private final SupportTicketMapper supportTicketMapper;
    private final NotificationMapper notificationMapper;
    private final FaqEntryMapper faqEntryMapper;

    //. -------------------- CREATE TICKET -------------------- .\\
    public SupportTicketResponseDTO createTicket(SupportTicketRequestDTO dto) {
        if (dto == null) {
            throw new BadRequestException("La demande de ticket est obligatoire.");
        }

        validateCreateTicket(dto);

        Account account = getAuthenticatedClientAccount();

        SupportTicket entity = new SupportTicket();
        entity.setAccount(account);

        // CLIENT côté compte => CITIZEN côté support ticket
        entity.setRoleType(SupportTicket.RoleType.CITIZEN);
        entity.setSubject(dto.getSubject().trim());
        entity.setMessage(dto.getDescription().trim());

        // status sera OPEN via @PrePersist si null
        entity.setStatus(SupportTicket.Status.OPEN);

        // createdBy / responseMessage / respondedAt restent null au départ
        SupportTicket saved = supportTicketRepository.save(entity);

        return supportTicketMapper.toResponseDTO(saved);
    }

    //. -------------------- MY TICKETS -------------------- .\\
    @Transactional(readOnly = true)
    public List<SupportTicketSummaryDTO> getMyTickets() {
        Account account = getAuthenticatedClientAccount();

        return supportTicketRepository.findByAccountIdOrderByCreatedAtDesc(account.getId())
                .stream()
                .map(supportTicketMapper::toSummaryDTO)
                .toList();
    }

    //. -------------------- MY TICKET BY ID -------------------- .\\
    @Transactional(readOnly = true)
    public SupportTicketResponseDTO getMyTicketById(Long ticketId) {
        if (ticketId == null) {
            throw new BadRequestException("L'identifiant du ticket est obligatoire.");
        }

        SupportTicket ticket = getOwnedTicket(ticketId);
        return supportTicketMapper.toResponseDTO(ticket);
    }

    //. -------------------- MY TICKET RESPONSE -------------------- .\\
    @Transactional(readOnly = true)
    public TicketResponseView getTicketResponse(Long ticketId) {
        if (ticketId == null) {
            throw new BadRequestException("L'identifiant du ticket est obligatoire.");
        }

        SupportTicket ticket = getOwnedTicket(ticketId);

        return TicketResponseView.builder()
                .ticketId(ticket.getId())
                .subject(ticket.getSubject())
                .status(ticket.getStatus() != null ? ticket.getStatus().name() : null)
                .responseMessage(ticket.getResponseMessage())
                .respondedAt(ticket.getRespondedAt())
                .hasResponse(ticket.getResponseMessage() != null && !ticket.getResponseMessage().isBlank())
                .build();
    }

    //. -------------------- MY NOTIFICATIONS -------------------- .\\
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getMyNotifications() {
        Account account = getAuthenticatedClientAccount();
        List<Notification> notifications = notificationRepository.findForAccount(
                account.getId(),
                mapRoleToNotificationRole(account.getRoleType())
        );
        return notifications.stream().map(notificationMapper::toDto).toList();
    }

    private Notification.RoleTypeN mapRoleToNotificationRole(Account.RoleType role) {
        if (role == Account.RoleType.CLIENT) return Notification.RoleTypeN.CITIZEN;
        if (role == Account.RoleType.COLLECTOR) return Notification.RoleTypeN.COLLECTOR;
        return null; // FACTORY_USER n’est pas diffusé
    }

    //. -------------------- CLIENT FAQ -------------------- .\\
    @Transactional(readOnly = true)
    public List<FaqEntrySummaryDTO> getClientFaq() {
        return faqEntryRepository.findAll()
                .stream()
                .filter(this::isVisibleForClient)
                .sorted(Comparator
                        .comparing(FaqEntry::getDisplayOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(FaqEntry::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(faqEntryMapper::toSummaryDto)
                .toList();
    }


    // Dans ClientSupportService.java

    @Transactional
    public void markNotificationAsRead(Long notificationId) {
        Account account = getAuthenticatedClientAccount();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification introuvable"));
        if (!notification.getAccount().getId().equals(account.getId())) {
            throw new UnauthorizedException("Cette notification ne vous appartient pas");
        }
        notificationRepository.markAsRead(notificationId);
    }

    @Transactional
    public void markAllNotificationsAsRead() {
        Account account = getAuthenticatedClientAccount();
        notificationRepository.markAllAsRead(account.getId());
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private Account getAuthenticatedClientAccount() {
        String email = SecurityUtils.getEmail()
                .orElseThrow(() -> new UnauthorizedException("Utilisateur non authentifié."));

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable."));

        if (account.getRoleType() != Account.RoleType.CLIENT) {
            throw new UnauthorizedException("Accès réservé au client.");
        }

        if (account.getClient() == null) {
            throw new ResourceNotFoundException("Profil client introuvable.");
        }

        return account;
    }

    private SupportTicket getOwnedTicket(Long ticketId) {
        Account account = getAuthenticatedClientAccount();

        return supportTicketRepository.findByIdAndAccountId(ticketId, account.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket support introuvable."));
    }

    private void validateCreateTicket(SupportTicketRequestDTO dto) {
        if (dto.getSubject() == null || dto.getSubject().isBlank()) {
            throw new BadRequestException("Le sujet est obligatoire.");
        }

        if (dto.getDescription() == null || dto.getDescription().isBlank()) {
            throw new BadRequestException("La description est obligatoire.");
        }

        if (dto.getSubject().trim().length() > 150) {
            throw new BadRequestException("Le sujet ne peut pas dépasser 150 caractères.");
        }

        if (dto.getDescription().trim().length() < 5 || dto.getDescription().trim().length() > 4000) {
            throw new BadRequestException("Le message doit être entre 5 et 4000 caractères.");
        }
    }

    private boolean isVisibleForClient(FaqEntry faq) {
        if (faq == null) {
            return false;
        }

        boolean active = faq.getStatus() == null || faq.getStatus() == FaqEntry.Status.ACTIVE;

        // ✅ Afficher si rôle = CITIZEN OU ALL
        boolean roleOk = faq.getRoleType() == null
                || faq.getRoleType() == FaqEntry.RoleType.CITIZEN
                || faq.getRoleType() == FaqEntry.RoleType.ALL;

        return active && roleOk;
    }

    // =========================================================
    // RESPONSE DTO INTERNE
    // =========================================================

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class TicketResponseView {
        private Long ticketId;
        private String subject;
        private String status;
        private String responseMessage;
        private java.time.LocalDateTime respondedAt;
        private Boolean hasResponse;
    }
}