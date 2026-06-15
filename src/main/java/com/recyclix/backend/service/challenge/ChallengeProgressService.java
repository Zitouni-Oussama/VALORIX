package com.recyclix.backend.service.challenge;

import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.model.*;
import com.recyclix.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChallengeProgressService {

    private final UserChallengeRepository userChallengeRepository;
    private final ChallengeHistoryRepository challengeHistoryRepository;
    private final CollectionRepository collectionRepository;
    private final PointMovementRepository pointMovementRepository;
    private final WalletRepository walletRepository;
    private final ChallengeRepository challengeRepository;
    private final AccountRepository accountRepository;

    // ============================================================
    // 1. MISE À JOUR APRÈS UNE COLLECTE
    // ============================================================

    @Transactional
    public void updateProgressAfterCollection(Collection collection) {
        if (collection == null || collection.getRequest() == null) {
            log.warn("Collection sans request, impossible de mettre à jour les défis");
            return;
        }

        Long accountId = collection.getRequest().getClient().getAccount().getId();
        Long materialId = collection.getRequest().getMaterial() != null
                ? collection.getRequest().getMaterial().getId()
                : null;
        BigDecimal quantity = collection.getRealQuantity();

        log.info("Mise à jour des défis pour l'utilisateur {}", accountId);

        List<UserChallenge> activeChallenges = userChallengeRepository.findActiveChallengesForUser(accountId);

        for (UserChallenge userChallenge : activeChallenges) {
            Challenge challenge = userChallenge.getChallenge();

            if (!challenge.isAvailableForMaterial(materialId)) {
                continue;
            }

            UserChallenge locked = userChallengeRepository.lockByAccountAndChallenge(
                    accountId, challenge.getId()
            ).orElse(userChallenge);

            int oldProgress = locked.getProgressQuantity();
            int increment = challenge.calculateProgressIncrement(quantity, materialId, true, locked.getCurrentStreak());

            if (increment > 0) {
                locked.addProgress(increment, collection.getId(), ChallengeHistory.ChangeReason.COLLECTION_COMPLETED);
                saveHistory(locked, oldProgress, locked.getProgressQuantity(), increment,
                        ChallengeHistory.ChangeReason.COLLECTION_COMPLETED, collection);
                userChallengeRepository.save(locked);

                if (locked.getStatus() == UserChallenge.ChallengeStatus.COMPLETED && !locked.getPointsAwarded()) {
                    awardChallengeReward(locked);
                }
            }
        }
    }

    // ============================================================
    // 2. ATTRIBUTION DES RÉCOMPENSES
    // ============================================================

    @Transactional
    public void awardChallengeReward(UserChallenge userChallenge) {
        if (userChallenge.getPointsAwarded()) {
            return;
        }

        Challenge challenge = userChallenge.getChallenge();
        Account account = userChallenge.getAccount();
        Wallet wallet = account.getWallet();

        if (wallet == null) {
            log.error("Wallet introuvable pour l'utilisateur {}", account.getId());
            return;
        }

        int totalPoints = challenge.getRewardPoints() +
                (challenge.getBonusPoints() != null ? challenge.getBonusPoints() : 0);

        wallet.addPoints(totalPoints);
        walletRepository.save(wallet);

        PointMovement movement = PointMovement.builder()
                .account(account)
                .type(PointMovement.PointMovementType.CHALLENGE_REWARD)
                .pointsAmount(totalPoints)
                .build();
        pointMovementRepository.save(movement);

        userChallenge.markPointsAwarded();
        userChallengeRepository.save(userChallenge);

        log.info("Récompense attribuée: {} points", totalPoints);
    }

    // ============================================================
    // 3. REJOINDRE UN DÉFI
    // ============================================================

    @Transactional
    public UserChallenge joinChallenge(Long accountId, Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResourceNotFoundException("Défi introuvable"));

        if (!challenge.isCurrentlyActive()) {
            throw new BadRequestException("Ce défi n'est pas actif actuellement");
        }

        if (userChallengeRepository.existsByAccountIdAndChallengeId(accountId, challengeId)) {
            throw new BadRequestException("Vous avez déjà rejoint ce défi");
        }

        UserChallenge userChallenge = UserChallenge.builder()
                .account(accountRepository.findById(accountId).orElseThrow())
                .challenge(challenge)
                .progressQuantity(0)
                .status(UserChallenge.ChallengeStatus.IN_PROGRESS)
                .currentStreak(0)
                .bestStreak(0)
                .pointsAwarded(false)
                .build();

        checkExistingProgress(userChallenge);
        return userChallengeRepository.save(userChallenge);
    }

    private void checkExistingProgress(UserChallenge userChallenge) {
        Challenge challenge = userChallenge.getChallenge();
        Long accountId = userChallenge.getAccount().getId();

        List<Collection> pastCollections = collectionRepository.findAll().stream()
                .filter(c -> c.getRequest() != null)
                .filter(c -> c.getRequest().getClient().getAccount().getId().equals(accountId))
                .filter(c -> c.getRequest().getStatus() == CollectionRequest.Status.COLLECTED)
                .toList();

        int totalProgress = 0;
        for (Collection collection : pastCollections) {
            Long materialId = collection.getRequest().getMaterial() != null
                    ? collection.getRequest().getMaterial().getId() : null;
            BigDecimal quantity = collection.getRealQuantity();

            if (challenge.isAvailableForMaterial(materialId)) {
                totalProgress += challenge.calculateProgressIncrement(quantity, materialId, true, 0);
            }
        }

        if (totalProgress > 0) {
            userChallenge.setProgressQuantity(Math.min(totalProgress, challenge.getTargetValue()));
            if (userChallenge.getProgressQuantity() >= challenge.getTargetValue()) {
                userChallenge.setStatus(UserChallenge.ChallengeStatus.COMPLETED);
            }
        }
    }

    // ============================================================
    // 4. STATISTIQUES
    // ============================================================

    @Transactional(readOnly = true)
    public ChallengeStatsDTO getUserChallengeStats(Long accountId) {
        long completedCount = userChallengeRepository.countByAccountIdAndStatus(
                accountId, UserChallenge.ChallengeStatus.COMPLETED);
        long inProgressCount = userChallengeRepository.countByAccountIdAndStatus(
                accountId, UserChallenge.ChallengeStatus.IN_PROGRESS);
        Integer totalPointsEarned = userChallengeRepository.sumEarnedPointsByAccountId(accountId);

        return ChallengeStatsDTO.builder()
                .completedChallenges(completedCount)
                .inProgressChallenges(inProgressCount)
                .totalPointsEarned(totalPointsEarned != null ? totalPointsEarned : 0)
                .build();
    }

    // ============================================================
    // 5. HISTORIQUE
    // ============================================================

    private void saveHistory(UserChallenge userChallenge, int oldProgress, int newProgress,
                             int changeAmount, ChallengeHistory.ChangeReason reason, Collection collection) {
        ChallengeHistory history = ChallengeHistory.builder()
                .userChallenge(userChallenge)
                .oldProgress(oldProgress)
                .newProgress(newProgress)
                .changeAmount(changeAmount)
                .changeReason(reason)
                .collection(collection)
                .build();
        challengeHistoryRepository.save(history);
    }

    // ============================================================
    // 6. UPDATE STREAKS (appelé par scheduler)
    // ============================================================

    @Transactional
    public void updateDailyStreaks() {
        log.info("Mise à jour quotidienne des streaks");
    }

    // ============================================================
    // 7. DTOs INTERNES
    // ============================================================

    @lombok.Data
    @lombok.Builder
    public static class ChallengeStatsDTO {
        private long completedChallenges;
        private long inProgressChallenges;
        private int totalPointsEarned;
    }
}