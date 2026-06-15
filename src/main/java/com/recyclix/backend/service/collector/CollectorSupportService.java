package com.recyclix.backend.service.collector;

import com.recyclix.backend.dto.support_ticket.SupportTicketRequestDTO;
import com.recyclix.backend.dto.support_ticket.SupportTicketResponseDTO;
import com.recyclix.backend.dto.support_ticket.SupportTicketSummaryDTO;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.exception.UnauthorizedException;
import com.recyclix.backend.mapper.SupportTicketMapper;
import com.recyclix.backend.model.Account;
import com.recyclix.backend.model.Notification;
import com.recyclix.backend.model.SupportTicket;
import com.recyclix.backend.repository.AccountRepository;
import com.recyclix.backend.repository.SupportTicketRepository;
import com.recyclix.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CollectorSupportService {

    private final AccountRepository accountRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final SupportTicketMapper supportTicketMapper;

    //. -------------------- CREATE TICKET -------------------- .\\
    public SupportTicketResponseDTO createTicket(SupportTicketRequestDTO dto) {
        validateCreateTicketInput(dto);

        Account account = getAuthenticatedCollectorAccount();

        SupportTicket ticket = supportTicketMapper.toEntity(dto);
        ticket.setAccount(account);
        ticket.setRoleType(SupportTicket.RoleType.COLLECTOR);
        ticket.setStatus(SupportTicket.Status.OPEN);

        SupportTicket saved = supportTicketRepository.save(ticket);
        return supportTicketMapper.toResponseDTO(saved);
    }

    //. -------------------- GET MY TICKETS -------------------- .\\
    @Transactional(readOnly = true)
    public List<SupportTicketSummaryDTO> getMyTickets() {
        Account account = getAuthenticatedCollectorAccount();

        return supportTicketRepository.findAllByAccountIdOrderByCreatedAtDesc(account.getId())
                .stream()
                .map(supportTicketMapper::toSummaryDTO)
                .toList();
    }

    //. -------------------- GET MY TICKET BY ID -------------------- .\\
    @Transactional(readOnly = true)
    public SupportTicketResponseDTO getMyTicketById(Long ticketId) {
        if (ticketId == null) {
            throw new BadRequestException("L'identifiant du ticket est obligatoire.");
        }

        Account account = getAuthenticatedCollectorAccount();

        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket introuvable."));

        if (ticket.getAccount() == null || !ticket.getAccount().getId().equals(account.getId())) {
            throw new UnauthorizedException("Ce ticket n'appartient pas au collecteur connecté.");
        }

        return supportTicketMapper.toResponseDTO(ticket);
    }

    //* =========================================================
    //! HELPERS
    //* =========================================================
    private Account getAuthenticatedCollectorAccount() {
        String email = SecurityUtils.getEmail()
                .orElseThrow(() -> new UnauthorizedException("Utilisateur non authentifié."));

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable."));

        if (account.getRoleType() != Account.RoleType.COLLECTOR) {
            throw new UnauthorizedException("Accès réservé au collecteur.");
        }

        return account;
    }

    private void validateCreateTicketInput(SupportTicketRequestDTO dto) {
        if (dto == null) {
            throw new BadRequestException("Les données du ticket sont obligatoires.");
        }

        if (!StringUtils.hasText(dto.getSubject())) {
            throw new BadRequestException("Le sujet du ticket est obligatoire.");
        }

        if (!StringUtils.hasText(dto.getDescription())) {
            throw new BadRequestException("La description du ticket est obligatoire.");
        }

        if (!StringUtils.hasText(dto.getPriority())) {
            throw new BadRequestException("La priorité du ticket est obligatoire.");
        }

        // accountId ne doit jamais piloter la sécurité ici
        if (dto.getAccountId() != null) {
            dto.setAccountId(null);
        }
    }

}