package com.recyclix.backend.service.support;

import com.recyclix.backend.dto.support.AnonymousTicketRequestDTO;
import com.recyclix.backend.model.SupportTicket;
import com.recyclix.backend.repository.SupportTicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnonymousTicketService {

    private final SupportTicketRepository supportTicketRepository;

    @Transactional
    public void createResetPasswordTicket(AnonymousTicketRequestDTO request) {
        String subject = "[AUTO] Demande de réinitialisation - Email inconnu";

        // Log pour déboguer
        log.info("Création ticket anonyme - roleType reçu: '{}'", request.getRoleType());

        SupportTicket.RoleType roleType = SupportTicket.RoleType.CITIZEN; // défaut
        if (request.getRoleType() != null && !request.getRoleType().isBlank()) {
            try {
                roleType = SupportTicket.RoleType.valueOf(request.getRoleType().trim().toUpperCase());
                log.info("Rôle converti avec succès: {}", roleType);
            } catch (IllegalArgumentException e) {
                log.warn("Valeur roleType invalide: '{}', utilisation de CITIZEN", request.getRoleType());
            }
        } else {
            log.warn("Aucun roleType fourni, utilisation de CITIZEN");
        }

        String roleValue = (request.getRoleType() != null) ? request.getRoleType() : "Non précisé";
        String message = String.format("""
        Un utilisateur a tenté de réinitialiser son mot de passe mais son email n'existe pas dans la base.
        
        Informations fournies :
        - Email : %s
        - Nom : %s
        - Téléphone : %s
        - Rôle : %s
        
        Date : %s
        """,
                request.getEmail() != null ? request.getEmail() : "Non renseigné",
                request.getFullName() != null && !request.getFullName().isBlank() ? request.getFullName() : "Non renseigné",
                request.getPhone() != null && !request.getPhone().isBlank() ? request.getPhone() : "Non renseigné",
                roleValue,
                LocalDateTime.now()
        );

        SupportTicket ticket = SupportTicket.builder()
                .account(null)
                .subject(subject)
                .message(message)
                .status(SupportTicket.Status.OPEN)
                .roleType(roleType)   // ← valeur calculée
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        supportTicketRepository.save(ticket);
        log.info("Ticket anonyme créé avec roleType = {}", roleType);
    }

    @Transactional
    public void createDisabledAccountTicket(AnonymousTicketRequestDTO request) {
        String subject = "[AUTO] Compte désactivé - Demande d'aide";

        log.info("Création ticket compte désactivé - roleType reçu: '{}'", request.getRoleType());

        SupportTicket.RoleType roleType = SupportTicket.RoleType.CITIZEN;
        if (request.getRoleType() != null && !request.getRoleType().isBlank()) {
            try {
                roleType = SupportTicket.RoleType.valueOf(request.getRoleType().trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Valeur roleType invalide: '{}', utilisation de CITIZEN", request.getRoleType());
            }
        }

        String message = String.format("""
    Un utilisateur a tenté de se connecter avec un compte désactivé.
    
    Informations fournies :
    - Email : %s
    - Nom : %s
    - Téléphone : %s
    - Rôle : %s
    
    Date : %s
    """,
                request.getEmail() != null ? request.getEmail() : "Non renseigné",
                request.getFullName() != null && !request.getFullName().isBlank() ? request.getFullName() : "Non renseigné",
                request.getPhone() != null && !request.getPhone().isBlank() ? request.getPhone() : "Non renseigné",
                request.getRoleType() != null ? request.getRoleType() : "Non précisé",
                LocalDateTime.now()
        );

        SupportTicket ticket = SupportTicket.builder()
                .account(null)
                .subject(subject)
                .message(message)
                .status(SupportTicket.Status.OPEN)
                .roleType(roleType)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        supportTicketRepository.save(ticket);
        log.info("Ticket compte désactivé créé avec roleType = {}", roleType);
    }
}