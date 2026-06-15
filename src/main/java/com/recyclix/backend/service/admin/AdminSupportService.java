// recyclix\backend\service\admin\AdminSupportService.java
package com.recyclix.backend.service.admin;

import com.recyclix.backend.dto.admin.*;
import com.recyclix.backend.dto.faq_entry.*;
import com.recyclix.backend.dto.support_ticket.*;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.exception.UnauthorizedException;
import com.recyclix.backend.mapper.FaqEntryMapper;
import com.recyclix.backend.mapper.SupportTicketMapper;
import com.recyclix.backend.model.*;
import com.recyclix.backend.repository.*;
import com.recyclix.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminSupportService {

    private final SupportTicketRepository ticketRepo;
    private final FaqEntryRepository faqEntryRepo;
    private final AccountRepository accountRepo;
    private final FactoryUserRepository factoryUserRepo;

    private final SupportTicketMapper ticketMapper;
    private final FaqEntryMapper faqMapper;

    // ============================================================
    // 1. STATISTIQUES SUPPORT
    // ============================================================
    @Transactional(readOnly = true)
    public AdminSupportStatsDTO getSupportStats() {

        List<SupportTicket> allTickets = ticketRepo.findAll();
        List<FaqEntry> allFaqs = faqEntryRepo.findAll();

        long totalTickets = allTickets.size();
        long openTickets = allTickets.stream()
                .filter(t -> t.getStatus() == SupportTicket.Status.OPEN)
                .count();
        long inProgressTickets = allTickets.stream()
                .filter(t -> t.getStatus() == SupportTicket.Status.IN_PROGRESS)
                .count();
        long closedTickets = allTickets.stream()
                .filter(t -> t.getStatus() == SupportTicket.Status.CLOSED)
                .count();

        long unassignedTickets = allTickets.stream()
                .filter(t -> t.getCreatedBy() == null)
                .count();

        long totalFaqs = allFaqs.size();
        long activeFaqs = allFaqs.stream()
                .filter(f -> f.getStatus() == FaqEntry.Status.ACTIVE)
                .count();

        long citizenTickets = allTickets.stream()
                .filter(t -> t.getRoleType() == SupportTicket.RoleType.CITIZEN)
                .count();
        long collectorTickets = allTickets.stream()
                .filter(t -> t.getRoleType() == SupportTicket.RoleType.COLLECTOR)
                .count();

        return AdminSupportStatsDTO.builder()
                .totalTickets(totalTickets)
                .openTickets(openTickets)
                .inProgressTickets(inProgressTickets)
                .closedTickets(closedTickets)
                .unassignedTickets(unassignedTickets)
                .totalFaqs(totalFaqs)
                .activeFaqs(activeFaqs)
                .citizenTickets(citizenTickets)
                .collectorTickets(collectorTickets)
                .build();
    }

    // ============================================================
    // 2. LISTER TOUS LES TICKETS (FILTRES + PAGINATION)
    // ============================================================
    @Transactional(readOnly = true)
    public Page<SupportTicketSummaryDTO> getAllTickets(
            SupportTicket.Status status,
            SupportTicket.RoleType roleType,
            Long accountId,
            int page,
            int size
    ) {
        List<SupportTicket> tickets = ticketRepo.findAll();

        List<SupportTicket> filtered = tickets.stream()
                .filter(t -> status == null || t.getStatus() == status)
                .filter(t -> roleType == null || t.getRoleType() == roleType)
                .filter(t -> accountId == null || (t.getAccount() != null && t.getAccount().getId().equals(accountId)))
                .sorted(Comparator.comparing(SupportTicket::getCreatedAt).reversed())
                .toList();

        int start = page * size;
        int end = Math.min(start + size, filtered.size());

        if (start >= filtered.size()) {
            return new PageImpl<>(List.of(), PageRequest.of(page, size), filtered.size());
        }

        List<SupportTicketSummaryDTO> dtos = filtered.subList(start, end).stream()
                .map(ticketMapper::toSummaryDTO)
                .toList();

        return new PageImpl<>(dtos, PageRequest.of(page, size), filtered.size());
    }

    // ============================================================
    // 3. DÉTAIL D'UN TICKET
    // ============================================================
    @Transactional(readOnly = true)
    public SupportTicketResponseDTO getTicketDetail(Long ticketId) {
        if (ticketId == null) {
            throw new BadRequestException("L'identifiant du ticket est obligatoire.");
        }

        SupportTicket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket introuvable avec l'ID : " + ticketId));

        return ticketMapper.toResponseDTO(ticket);
    }

    // ============================================================
    // 4. RÉPONDRE À UN TICKET
    // ============================================================
    public SupportTicketResponseDTO respondToTicket(Long ticketId, AdminTicketResponseRequestDTO request) {
        if (ticketId == null) {
            throw new BadRequestException("L'identifiant du ticket est obligatoire.");
        }

        if (request == null || request.getResponseMessage() == null || request.getResponseMessage().isBlank()) {
            throw new BadRequestException("La réponse est obligatoire.");
        }

        SupportTicket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket introuvable avec l'ID : " + ticketId));

        if (ticket.getStatus() == SupportTicket.Status.CLOSED) {
            throw new BadRequestException("Impossible de répondre à un ticket fermé.");
        }

        // Marquer IN_PROGRESS si encore OPEN
        if (ticket.getStatus() == SupportTicket.Status.OPEN) {
            ticket.setStatus(SupportTicket.Status.IN_PROGRESS);
        }

        ticket.setResponseMessage(request.getResponseMessage().trim());
        ticket.setRespondedAt(LocalDateTime.now());

        SupportTicket savedTicket = ticketRepo.save(ticket);
        return ticketMapper.toResponseDTO(savedTicket);
    }

    // ============================================================
    // 5. ASSIGNER UN TICKET À UN FACTORY USER
    // ============================================================
    public SupportTicketResponseDTO assignTicket(Long ticketId, AdminTicketAssignRequestDTO request) {
        if (ticketId == null) {
            throw new BadRequestException("L'identifiant du ticket est obligatoire.");
        }

        if (request == null || request.getAssignedToId() == null) {
            throw new BadRequestException("L'identifiant du FactoryUser assigné est obligatoire.");
        }

        SupportTicket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket introuvable avec l'ID : " + ticketId));

        FactoryUser assignedUser = factoryUserRepo.findById(request.getAssignedToId())
                .orElseThrow(() -> new ResourceNotFoundException("FactoryUser introuvable avec l'ID : " + request.getAssignedToId()));

        ticket.setCreatedBy(assignedUser);

        if (ticket.getStatus() == SupportTicket.Status.OPEN) {
            ticket.setStatus(SupportTicket.Status.IN_PROGRESS);
        }

        SupportTicket savedTicket = ticketRepo.save(ticket);
        return ticketMapper.toResponseDTO(savedTicket);
    }

    // ============================================================
    // 6. FERMER UN TICKET
    // ============================================================
    public SupportTicketResponseDTO closeTicket(Long ticketId) {
        if (ticketId == null) {
            throw new BadRequestException("L'identifiant du ticket est obligatoire.");
        }

        SupportTicket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket introuvable avec l'ID : " + ticketId));

        if (ticket.getStatus() == SupportTicket.Status.CLOSED) {
            throw new BadRequestException("Ce ticket est déjà fermé.");
        }

        ticket.setStatus(SupportTicket.Status.CLOSED);

        if (ticket.getRespondedAt() == null) {
            ticket.setRespondedAt(LocalDateTime.now());
        }

        SupportTicket savedTicket = ticketRepo.save(ticket);
        return ticketMapper.toResponseDTO(savedTicket);
    }

    // ============================================================
    // 7. RÉOUVRIR UN TICKET
    // ============================================================
    public SupportTicketResponseDTO reopenTicket(Long ticketId) {
        if (ticketId == null) {
            throw new BadRequestException("L'identifiant du ticket est obligatoire.");
        }

        SupportTicket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket introuvable avec l'ID : " + ticketId));

        if (ticket.getStatus() != SupportTicket.Status.CLOSED) {
            throw new BadRequestException("Seul un ticket fermé peut être rouvert.");
        }

        ticket.setStatus(SupportTicket.Status.IN_PROGRESS);

        SupportTicket savedTicket = ticketRepo.save(ticket);
        return ticketMapper.toResponseDTO(savedTicket);
    }

    // ============================================================
    // 8. LISTER LES FAQ (TOUTES, AVEC FILTRE)
    // ============================================================
    @Transactional(readOnly = true)
    public Page<FaqEntrySummaryDTO> getAllFaqs(
            FaqEntry.RoleType roleType,
            FaqEntry.Status status,
            int page,
            int size
    ) {
        List<FaqEntry> faqs = faqEntryRepo.findAll();

        List<FaqEntry> filtered = faqs.stream()
                .filter(f -> roleType == null || f.getRoleType() == roleType)
                .filter(f -> status == null || f.getStatus() == status)
                .sorted(Comparator
                        .comparing(FaqEntry::getCategoryLabel, Comparator.nullsLast(String::compareToIgnoreCase))
                        .thenComparing(FaqEntry::getDisplayOrder, Comparator.nullsLast(Integer::compareTo)))
                .toList();

        int start = page * size;
        int end = Math.min(start + size, filtered.size());

        if (start >= filtered.size()) {
            return new PageImpl<>(List.of(), PageRequest.of(page, size), filtered.size());
        }

        List<FaqEntrySummaryDTO> dtos = filtered.subList(start, end).stream()
                .map(faqMapper::toSummaryDto)
                .toList();

        return new PageImpl<>(dtos, PageRequest.of(page, size), filtered.size());
    }

    // ============================================================
    // 9. DÉTAIL D'UNE FAQ
    // ============================================================
    @Transactional(readOnly = true)
    public FaqEntryResponseDTO getFaqDetail(Long faqId) {
        if (faqId == null) {
            throw new BadRequestException("L'identifiant de la FAQ est obligatoire.");
        }

        FaqEntry faq = faqEntryRepo.findById(faqId)
                .orElseThrow(() -> new ResourceNotFoundException("FAQ introuvable avec l'ID : " + faqId));

        return faqMapper.toDto(faq);
    }

    // ============================================================
    // 10. CRÉER UNE FAQ
    // ============================================================
    // recyclix\backend\service\admin\AdminSupportService.java
// Modifier la méthode createFaq() :

    // recyclix\backend\service\admin\AdminSupportService.java

    public FaqEntryResponseDTO createFaq(FaqEntryRequestDTO request) {
        // Validation des champs obligatoires
        if (request == null) {
            throw new BadRequestException("Les données de la FAQ sont obligatoires.");
        }

        if (request.getQuestion() == null || request.getQuestion().isBlank()) {
            throw new BadRequestException("La question est obligatoire.");
        }

        if (request.getAnswer() == null || request.getAnswer().isBlank()) {
            throw new BadRequestException("La réponse est obligatoire.");
        }

        if (request.getRoleType() == null) {
            throw new BadRequestException("Le rôle cible (CITIZEN, COLLECTOR, ou ALL) est obligatoire.");
        }

        if (request.getCategoryKey() == null || request.getCategoryKey().isBlank()) {
            throw new BadRequestException("La catégorie est obligatoire.");
        }

        if (request.getCategoryLabel() == null || request.getCategoryLabel().isBlank()) {
            throw new BadRequestException("Le label de catégorie est obligatoire.");
        }

        // Récupération de l'utilisateur connecté
        Long accountId = SecurityUtils.getAccountId()
                .orElseThrow(() -> new UnauthorizedException("Utilisateur non authentifié."));

        FactoryUser currentUser = factoryUserRepo.findByAccountId(accountId)
                .orElse(null);

        // Création de l'entité via le mapper
        FaqEntry faq = faqMapper.toEntity(request);

        // ✅ Définition explicite des champs non mappés automatiquement
        faq.setRoleType(request.getRoleType());
        faq.setCategoryKey(request.getCategoryKey().trim());
        faq.setCategoryLabel(request.getCategoryLabel().trim());

        if (request.getDisplayOrder() != null) {
            faq.setDisplayOrder(request.getDisplayOrder());
        } else {
            faq.setDisplayOrder(0);
        }

        if (request.getStatus() != null) {
            faq.setStatus(request.getStatus());
        } else {
            faq.setStatus(FaqEntry.Status.ACTIVE);
        }

        faq.setCreatedBy(currentUser);

        FaqEntry savedFaq = faqEntryRepo.save(faq);
        return faqMapper.toDto(savedFaq);
    }

    // ============================================================
    // 11. MODIFIER UNE FAQ
    // ============================================================
    // recyclix\backend\service\admin\AdminSupportService.java
// Modifier la méthode updateFaq() :

    // recyclix\backend\service\admin\AdminSupportService.java

    public FaqEntryResponseDTO updateFaq(Long faqId, FaqEntryUpdateDTO request) {
        if (faqId == null) {
            throw new BadRequestException("L'identifiant de la FAQ est obligatoire.");
        }

        if (request == null) {
            throw new BadRequestException("Les données de mise à jour sont obligatoires.");
        }

        FaqEntry faq = faqEntryRepo.findById(faqId)
                .orElseThrow(() -> new ResourceNotFoundException("FAQ introuvable avec l'ID : " + faqId));

        Long accountId = SecurityUtils.getAccountId()
                .orElseThrow(() -> new UnauthorizedException("Utilisateur non authentifié."));

        FactoryUser currentUser = factoryUserRepo.findByAccountId(accountId)
                .orElse(null);

        // ✅ Mise à jour des champs texte
        if (request.getQuestion() != null && !request.getQuestion().isBlank()) {
            faq.setQuestion(request.getQuestion().trim());
        }

        if (request.getAnswer() != null && !request.getAnswer().isBlank()) {
            faq.setAnswer(request.getAnswer().trim());
        }

        // ✅ Mise à jour du rôle
        if (request.getRoleType() != null) {
            faq.setRoleType(request.getRoleType());
        }

        // ✅ Mise à jour de la catégorie
        if (request.getCategoryKey() != null && !request.getCategoryKey().isBlank()) {
            faq.setCategoryKey(request.getCategoryKey().trim());
        }

        // ✅ Mise à jour du label de catégorie
        if (request.getCategoryLabel() != null && !request.getCategoryLabel().isBlank()) {
            faq.setCategoryLabel(request.getCategoryLabel().trim());
        }

        // ✅ Mise à jour de l'ordre d'affichage
        if (request.getDisplayOrder() != null) {
            faq.setDisplayOrder(request.getDisplayOrder());
        }

        // ✅ Mise à jour du statut
        if (request.getStatus() != null) {
            faq.setStatus(request.getStatus());
        }

        // Mise à jour du créateur
        if (request.getCreatedById() != null) {
            faq.setCreatedBy(currentUser);
        }

        FaqEntry savedFaq = faqEntryRepo.save(faq);
        return faqMapper.toDto(savedFaq);
    }

    // ============================================================
    // 12. ACTIVER / DÉSACTIVER UNE FAQ
    // ============================================================
    public FaqEntryResponseDTO activateFaq(Long faqId) {
        FaqEntry faq = getFaqOrThrow(faqId);
        faq.setStatus(FaqEntry.Status.ACTIVE);
        return faqMapper.toDto(faqEntryRepo.save(faq));
    }

    public FaqEntryResponseDTO deactivateFaq(Long faqId) {
        FaqEntry faq = getFaqOrThrow(faqId);
        faq.setStatus(FaqEntry.Status.INACTIVE);
        return faqMapper.toDto(faqEntryRepo.save(faq));
    }

    // ============================================================
    // 13. SUPPRIMER UNE FAQ
    // ============================================================
    public void deleteFaq(Long faqId) {
        FaqEntry faq = getFaqOrThrow(faqId);
        faqEntryRepo.delete(faq);
    }

    // ============================================================
    // HELPER PRIVÉ
    // ============================================================
    private FaqEntry getFaqOrThrow(Long faqId) {
        if (faqId == null) {
            throw new BadRequestException("L'identifiant de la FAQ est obligatoire.");
        }

        return faqEntryRepo.findById(faqId)
                .orElseThrow(() -> new ResourceNotFoundException("FAQ introuvable avec l'ID : " + faqId));
    }
}