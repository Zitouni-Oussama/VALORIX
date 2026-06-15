package com.recyclix.backend.service.client;

import com.recyclix.backend.dto.challenge.ChallengeSummaryDTO;
import com.recyclix.backend.dto.challenge.TicketRequestChallengeResponse;
import com.recyclix.backend.dto.user_challenge.UserChallengeResponseDTO;
import com.recyclix.backend.dto.user_challenge.UserChallengeSummaryDTO;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ConflictException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.exception.UnauthorizedException;
import com.recyclix.backend.mapper.ChallengeMapper;
import com.recyclix.backend.mapper.UserChallengeMapper;
import com.recyclix.backend.model.Account;
import com.recyclix.backend.model.Challenge;
import com.recyclix.backend.model.SupportTicket;
import com.recyclix.backend.model.UserChallenge;
import com.recyclix.backend.repository.AccountRepository;
import com.recyclix.backend.repository.ChallengeRepository;
import com.recyclix.backend.repository.SupportTicketRepository;
import com.recyclix.backend.repository.UserChallengeRepository;
import com.recyclix.backend.service.challenge.ChallengeProgressService;
import com.recyclix.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientChallengeService {

    private final AccountRepository accountRepository;
    private final ChallengeRepository challengeRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final ChallengeProgressService challengeProgressService;

    private final ChallengeMapper challengeMapper;
    private final UserChallengeMapper userChallengeMapper;

    // ============================================================
    // 1. DÉFIS DISPONIBLES
    // ============================================================

    @Transactional(readOnly = true)
    public List<ChallengeSummaryDTO> getAvailableChallenges() {
        LocalDateTime now = LocalDateTime.now();
        return challengeRepository.findByIsActiveTrue()
                .stream()
                .filter(challenge -> challenge.isCurrentlyActive())
                .sorted(Comparator.comparing(Challenge::getCreatedAt).reversed())
                .map(challengeMapper::toSummaryDto)
                .toList();
    }

    // ============================================================
    // 2. REJOINDRE UN DÉFI
    // ============================================================

    public UserChallengeResponseDTO joinChallenge(Long challengeId) {
        if (challengeId == null) {
            throw new BadRequestException("L'identifiant du défi est obligatoire.");
        }

        Account account = getAuthenticatedClientAccount();
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResourceNotFoundException("Défi introuvable."));

        if (!challenge.isCurrentlyActive()) {
            throw new BadRequestException("Ce défi n'est plus disponible.");
        }

        boolean alreadyJoined = userChallengeRepository.existsByAccountIdAndChallengeId(account.getId(), challengeId);
        if (alreadyJoined) {
            throw new ConflictException("Vous avez déjà rejoint ce défi.");
        }

        UserChallenge saved = challengeProgressService.joinChallenge(account.getId(), challengeId);
        return userChallengeMapper.toResponseDTO(saved);
    }

    // ============================================================
    // 3. MES DÉFIS
    // ============================================================

    @Transactional(readOnly = true)
    public List<UserChallengeSummaryDTO> getMyChallenges() {
        Account account = getAuthenticatedClientAccount();
        return userChallengeRepository.findAllByAccountId(account.getId())
                .stream()
                .sorted(Comparator.comparing(UserChallenge::getCreatedAt).reversed())
                .map(userChallengeMapper::toSummaryDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserChallengeSummaryDTO> getMyCompletedChallenges() {
        Account account = getAuthenticatedClientAccount();
        return userChallengeRepository.findAllByAccountId(account.getId())
                .stream()
                .filter(uc -> uc.getStatus() == UserChallenge.ChallengeStatus.COMPLETED)
                .sorted(Comparator.comparing(UserChallenge::getCompletedAt).reversed())
                .map(userChallengeMapper::toSummaryDTO)
                .toList();
    }

    // ============================================================
    // 4. PROGRESSION D'UN DÉFI
    // ============================================================

    @Transactional(readOnly = true)
    public UserChallengeResponseDTO getMyChallengeProgress(Long userChallengeId) {
        if (userChallengeId == null) {
            throw new BadRequestException("L'identifiant du challenge utilisateur est obligatoire.");
        }
        UserChallenge entity = getOwnedUserChallenge(userChallengeId);
        return userChallengeMapper.toResponseDTO(entity);
    }

    // ============================================================
    // 5. DEMANDE DE VALIDATION (ticket support)
    // ============================================================

    public TicketRequestChallengeResponse requestChallengeValidation(Long userChallengeId) {
        if (userChallengeId == null) {
            throw new BadRequestException("L'identifiant du challenge utilisateur est obligatoire.");
        }

        Account account = getAuthenticatedClientAccount();
        UserChallenge userChallenge = getOwnedUserChallenge(userChallengeId);
        Challenge challenge = userChallenge.getChallenge();

        if (challenge == null) {
            throw new ResourceNotFoundException("Défi introuvable.");
        }

        if (userChallenge.getStatus() == UserChallenge.ChallengeStatus.COMPLETED) {
            throw new BadRequestException("Ce défi est déjà complété.");
        }

        String subject = "Demande de validation challenge #" + challenge.getId();

        String message = """
            Bonjour Admin,

            Je demande la validation de mon challenge.

            challengeId: %d
            userChallengeId: %d

            Titre challenge: %s
            Statut actuel: %s
            Date de participation: %s

            Merci de vérifier si le challenge est complété ou non.
            """.formatted(
                challenge.getId(),
                userChallenge.getId(),
                challenge.getTitle() != null ? challenge.getTitle() : "N/A",
                userChallenge.getStatus() != null ? userChallenge.getStatus().name() : "N/A",
                userChallenge.getCreatedAt() != null ? userChallenge.getCreatedAt().toString() : "N/A"
        );

        SupportTicket ticket = new SupportTicket();
        ticket.setAccount(account);
        ticket.setRoleType(SupportTicket.RoleType.CITIZEN);
        ticket.setSubject(subject);
        ticket.setMessage(message);
        ticket.setStatus(SupportTicket.Status.OPEN);

        SupportTicket savedTicket = supportTicketRepository.save(ticket);

        return TicketRequestChallengeResponse.builder()
                .ticketId(savedTicket.getId())
                .challengeId(challenge.getId())
                .userChallengeId(userChallenge.getId())
                .status("OPEN")
                .message("Ticket de validation du challenge créé avec succès.")
                .build();
    }

    // ============================================================
    // 6. STATISTIQUES DES RÉCOMPENSES
    // ============================================================

    @Transactional(readOnly = true)
    public RewardOverviewResponse getMyRewardsOverview() {
        Account account = getAuthenticatedClientAccount();
        ChallengeProgressService.ChallengeStatsDTO stats = challengeProgressService.getUserChallengeStats(account.getId());
        List<UserChallengeSummaryDTO> completedChallenges = getMyCompletedChallenges();

        return RewardOverviewResponse.builder()
                .completedChallengesCount((int) stats.getCompletedChallenges())
                .totalEarnedRewardPoints(stats.getTotalPointsEarned())
                .completedChallenges(completedChallenges)
                .build();
    }

    // ============================================================
    // 7. HELPERS
    // ============================================================

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

    private UserChallenge getOwnedUserChallenge(Long userChallengeId) {
        Account account = getAuthenticatedClientAccount();
        return userChallengeRepository.findByAccountIdAndId(account.getId(), userChallengeId)
                .orElseThrow(() -> new ResourceNotFoundException("Défi utilisateur introuvable."));
    }

    // ============================================================
    // 8. RESPONSE DTO
    // ============================================================

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class RewardOverviewResponse {
        private Integer completedChallengesCount;
        private Integer totalEarnedRewardPoints;
        private List<UserChallengeSummaryDTO> completedChallenges;
    }
}