package com.recyclix.backend.service.client;

import com.recyclix.backend.controller.client.ClientRewardController;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.exception.UnauthorizedException;
import com.recyclix.backend.model.Account;
import com.recyclix.backend.model.PointMovement;
import com.recyclix.backend.model.Reward;
import com.recyclix.backend.model.Reward.RewardCategory;
import com.recyclix.backend.model.RewardRedemption;
import com.recyclix.backend.model.RewardRedemption.RedemptionStatus;
import com.recyclix.backend.model.Wallet;
import com.recyclix.backend.repository.AccountRepository;
import com.recyclix.backend.repository.PointMovementRepository;
import com.recyclix.backend.repository.RewardRedemptionRepository;
import com.recyclix.backend.repository.RewardRepository;
import com.recyclix.backend.repository.WalletRepository;
import com.recyclix.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientRewardService {

    private final AccountRepository accountRepository;
    private final WalletRepository walletRepository;
    private final RewardRepository rewardRepository;
    private final RewardRedemptionRepository redemptionRepository;
    private final PointMovementRepository pointMovementRepository;

    //. -------------------- CATALOGUE -------------------- .\\

    @Transactional(readOnly = true)
    public List<RewardSummaryResponse> getAvailableRewards() {
        getAuthenticatedClientAccount();  // vérifie juste l'authentification

        List<Reward> allRewards = rewardRepository.findAllAvailable();

        // Regrouper par catégorie
        return allRewards.stream()
                .map(this::toRewardDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public RewardDetailResponse getRewardDetail(Long rewardId) {
        if (rewardId == null) {
            throw new BadRequestException("L'identifiant de la récompense est obligatoire.");
        }

        Account account = getAuthenticatedClientAccount();
        Wallet wallet = getWallet(account);

        Reward reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> new ResourceNotFoundException("Récompense introuvable."));

        if (!Boolean.TRUE.equals(reward.getIsActive())) {
            throw new ResourceNotFoundException("Cette récompense n'est plus disponible.");
        }

        boolean canAfford = wallet.getBalancePoints() >= reward.getPointsCost();
        boolean inStock = reward.isInStock();

        return RewardDetailResponse.builder()
                .id(reward.getId())
                .name(reward.getName())
                .description(reward.getDescription())
                .pointsCost(reward.getPointsCost())
                .monetaryValue(reward.getMonetaryValue())
                .category(reward.getCategory() != null ? reward.getCategory().name() : null)
                .imageUrl(reward.getImageUrl())
                .stock(reward.getStock())
                .partnerName(reward.getPartnerName())
                .canAfford(canAfford)
                .isInStock(inStock)
                .yourBalance(wallet.getBalancePoints())
                .build();
    }

    @Transactional(readOnly = true)
    public List<RewardSummaryResponse> getRewardsByCategory(String category) {
        getAuthenticatedClientAccount();

        RewardCategory rewardCategory;
        try {
            rewardCategory = RewardCategory.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Catégorie invalide : " + category);
        }

        return rewardRepository.findAvailableByCategory(rewardCategory)
                .stream()
                .map(this::toRewardDto)
                .toList();
    }

    //. -------------------- ÉCHANGE -------------------- .\\

//    public RewardRedemptionResponse redeemReward(Long rewardId, Object request) {
//        if (rewardId == null) {
//            throw new BadRequestException("L'identifiant de la récompense est obligatoire.");
//        }
//
//        Account account = getAuthenticatedClientAccount();
//        Wallet wallet = getWallet(account);
//
//        // Lock pour éviter les race conditions
//        Reward reward = rewardRepository.lockById(rewardId)
//                .orElseThrow(() -> new ResourceNotFoundException("Récompense introuvable."));
//
//        // 1. Vérifier que la récompense est active
//        if (!Boolean.TRUE.equals(reward.getIsActive())) {
//            throw new BadRequestException("Cette récompense n'est plus disponible.");
//        }
//
//        // 2. Vérifier le stock
//        if (!reward.isInStock()) {
//            throw new BadRequestException("Cette récompense est en rupture de stock.");
//        }
//
//        // 3. Vérifier les points
//        Integer userPoints = wallet.getBalancePoints();
//        if (userPoints == null || userPoints < reward.getPointsCost()) {
//            throw new BadRequestException(
//                    String.format("Points insuffisants. Vous avez %d points, il en faut %d.",
//                            userPoints != null ? userPoints : 0,
//                            reward.getPointsCost())
//            );
//        }
//
//        // 4. Vérifier qu'il n'a pas déjà une demande en attente pour la même récompense
//        boolean alreadyPending = redemptionRepository.existsByAccountIdAndRewardIdAndStatus(
//                account.getId(), reward.getId(), RedemptionStatus.PENDING
//        );
//        if (alreadyPending) {
//            throw new BadRequestException("Vous avez déjà une demande en attente pour cette récompense.");
//        }
//
//        // 5. Décrémenter le stock (si pas illimité)
//        reward.decrementStock();
//        rewardRepository.save(reward);
//
//        // 6. Déduire les points
//        wallet.setBalancePoints(userPoints - reward.getPointsCost());
//        walletRepository.save(wallet);
//
//        // 7. Créer le mouvement de points
//        PointMovement pointMovement = PointMovement.builder()
//                .account(account)
//                .collection(null)
//                .type(PointMovement.PointMovementType.CONVERT)
//                .pointsAmount(reward.getPointsCost())
//                .build();
//        pointMovementRepository.save(pointMovement);
//
//        // 8. Créer la demande d'échange
//        RewardRedemption redemption = RewardRedemption.builder()
//                .account(account)
//                .reward(reward)
//                .pointsSpent(reward.getPointsCost())
//                .status(RedemptionStatus.PENDING)
//                .expiryDate(reward.getValidityDays() != null
//                        ? LocalDateTime.now().plusDays(reward.getValidityDays())
//                        : null)
//                .build();
//
//        RewardRedemption saved = redemptionRepository.save(redemption);
//
//        return toRedemptionResponse(saved);
//    }

//    @Transactional
//    public RewardRedemptionResponse redeemReward(Long rewardId, Object request) {
//        if (rewardId == null) {
//            throw new BadRequestException("L'identifiant de la récompense est obligatoire.");
//        }
//
//        Account account = getAuthenticatedClientAccount();
//        Wallet wallet = getWallet(account);
//
//        // Verrouillage pour éviter les concurrences
//        Reward reward = rewardRepository.lockById(rewardId)
//                .orElseThrow(() -> new ResourceNotFoundException("Récompense introuvable."));
//
//        // 1. Vérifier que la récompense est active
//        if (!Boolean.TRUE.equals(reward.getIsActive())) {
//            throw new BadRequestException("Cette récompense n'est plus disponible.");
//        }
//
//        // 2. Vérifier le stock
//        if (!reward.isInStock()) {
//            throw new BadRequestException("Cette récompense est en rupture de stock.");
//        }
//
//        // 3. Vérifier les points
//        Integer userPoints = wallet.getBalancePoints();
//        if (userPoints == null || userPoints < reward.getPointsCost()) {
//            throw new BadRequestException(String.format(
//                    "Points insuffisants. Vous avez %d points, il en faut %d.",
//                    userPoints != null ? userPoints : 0, reward.getPointsCost()));
//        }
//
//        // 4. Vérifier l'argent disponible (taux 1 point = 0,5 DA)
//        BigDecimal pointValue = new BigDecimal("0.5");
//        BigDecimal moneyRequired = BigDecimal.valueOf(reward.getPointsCost()).multiply(pointValue);
//        BigDecimal currentMoney = wallet.getBalanceMoney() == null ? BigDecimal.ZERO : wallet.getBalanceMoney();
//        if (currentMoney.compareTo(moneyRequired) < 0) {
//            throw new BadRequestException(String.format(
//                    "Solde d'argent insuffisant. Vous avez %.2f DA, il vous faut %.2f DA.",
//                    currentMoney, moneyRequired));
//        }
//
//        // 5. Vérifier qu'il n'a pas déjà une demande en attente pour cette récompense
////        boolean alreadyPending = redemptionRepository.existsByAccountIdAndRewardIdAndStatus(
////                account.getId(), reward.getId(), RedemptionStatus.PENDING);
////        if (alreadyPending) {
////            throw new BadRequestException("Vous avez déjà une demande en attente pour cette récompense.");
////        }
//
//        // 6. Décrémenter le stock (sauf si illimité)
//        reward.decrementStock();
//        rewardRepository.save(reward);
//
//        // 7. Déduire les points
//        wallet.setBalancePoints(userPoints - reward.getPointsCost());
//
//        // 8. Déduire l'argent correspondant
//        wallet.setBalanceMoney(currentMoney.subtract(moneyRequired));
//        walletRepository.save(wallet);
//
//        // 9. Enregistrer le mouvement de points (CONVERT)
//        PointMovement pointMovement = PointMovement.builder()
//                .account(account)
//                .type(PointMovement.PointMovementType.CONVERT)
//                .pointsAmount(reward.getPointsCost())
//                .build();
//        pointMovementRepository.save(pointMovement);
//
//        // 10. Créer la demande d'échange
//        RewardRedemption redemption = RewardRedemption.builder()
//                .account(account)
//                .reward(reward)
//                .pointsSpent(reward.getPointsCost())
//                .status(RedemptionStatus.PENDING)
//                .expiryDate(reward.getValidityDays() != null
//                        ? LocalDateTime.now().plusDays(reward.getValidityDays())
//                        : null)
//                .build();
//
//        // Si la requête contient des infos supplémentaires (ex: numéro de téléphone)
//        if (request instanceof ClientRewardController.RedeemRequest) {
//            ClientRewardController.RedeemRequest req = (ClientRewardController.RedeemRequest) request;
//            if (req.getAdditionalInfo() != null) {
//                redemption.setAdditionalInfo(req.getAdditionalInfo());
//            }
//        }
//
//        RewardRedemption saved = redemptionRepository.save(redemption);
//        return toRedemptionResponse(saved);
//    }



    @Transactional
    public RewardRedemptionResponse redeemReward(Long rewardId, Object request) {
        if (rewardId == null) {
            throw new BadRequestException("L'identifiant de la récompense est obligatoire.");
        }

        Account account = getAuthenticatedClientAccount();
        Wallet wallet = getWallet(account);

        // Verrouillage pour éviter les concurrences
        Reward reward = rewardRepository.lockById(rewardId)
                .orElseThrow(() -> new ResourceNotFoundException("Récompense introuvable."));

        // 1. Vérifier que la récompense est active
        if (!Boolean.TRUE.equals(reward.getIsActive())) {
            throw new BadRequestException("Cette récompense n'est plus disponible.");
        }

        // 2. Vérifier le stock
        if (!reward.isInStock()) {
            throw new BadRequestException("Cette récompense est en rupture de stock.");
        }

        // 3. Vérifier les points
        Integer userPoints = wallet.getBalancePoints();
        if (userPoints == null || userPoints < reward.getPointsCost()) {
            throw new BadRequestException(String.format(
                    "Points insuffisants. Vous avez %d points, il en faut %d.",
                    userPoints != null ? userPoints : 0, reward.getPointsCost()));
        }

        // 4. Vérifier l'argent disponible (taux 1 point = 0,5 DA)
        BigDecimal pointValue = new BigDecimal("0.5");
        BigDecimal moneyRequired = BigDecimal.valueOf(reward.getPointsCost()).multiply(pointValue);
        BigDecimal currentMoney = wallet.getBalanceMoney() == null ? BigDecimal.ZERO : wallet.getBalanceMoney();
        if (currentMoney.compareTo(moneyRequired) < 0) {
            throw new BadRequestException(String.format(
                    "Solde d'argent insuffisant. Vous avez %.2f DA, il vous faut %.2f DA.",
                    currentMoney, moneyRequired));
        }

        // 5. Vérification de demande en attente – COMMENTÉE (suppression de la restriction)
        // boolean alreadyPending = redemptionRepository.existsByAccountIdAndRewardIdAndStatus(
        //         account.getId(), reward.getId(), RedemptionStatus.PENDING);
        // if (alreadyPending) {
        //     throw new BadRequestException("Vous avez déjà une demande en attente pour cette récompense.");
        // }

        // 6. Décrémenter le stock (sauf si illimité)
        reward.decrementStock();
        rewardRepository.save(reward);

        // 7. Déduire les points
        wallet.setBalancePoints(userPoints - reward.getPointsCost());

        // 8. Déduire l'argent correspondant
        wallet.setBalanceMoney(currentMoney.subtract(moneyRequired));
        walletRepository.save(wallet);

        // 9. Enregistrer le mouvement de points (CONVERT)
        PointMovement pointMovement = PointMovement.builder()
                .account(account)
                .type(PointMovement.PointMovementType.CONVERT)
                .pointsAmount(reward.getPointsCost())
                .build();
        pointMovementRepository.save(pointMovement);

        // 10. Créer la demande d'échange avec gestion du champ additionalInfo
        RewardRedemption.RewardRedemptionBuilder builder = RewardRedemption.builder()
                .account(account)
                .reward(reward)
                .pointsSpent(reward.getPointsCost())
                .status(RedemptionStatus.PENDING)
                .expiryDate(reward.getValidityDays() != null
                        ? LocalDateTime.now().plusDays(reward.getValidityDays())
                        : null);

        // Extraire les informations supplémentaires si la requête est du bon type
        String additionalInfo = null;
        if (request instanceof ClientRewardController.RedeemRequest) {
            additionalInfo = ((ClientRewardController.RedeemRequest) request).getAdditionalInfo();
            if (additionalInfo != null && !additionalInfo.isBlank()) {
                builder.additionalInfo(additionalInfo);
            }
        }

        RewardRedemption redemption = builder.build();
        RewardRedemption saved = redemptionRepository.save(redemption);

        return toRedemptionResponse(saved);
    }



    @Transactional(readOnly = true)
    public List<RewardRedemptionResponse> getMyRedemptions() {
        Account account = getAuthenticatedClientAccount();

        return redemptionRepository.findAllByAccountIdOrderByCreatedAtDesc(account.getId())
                .stream()
                .map(this::toRedemptionResponse)
                .toList();
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

        return account;
    }

    private Wallet getWallet(Account account) {
        return walletRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet introuvable."));
    }

    private RewardSummaryResponse toRewardDto(Reward reward) {
        return RewardSummaryResponse.builder()
                .id(reward.getId())
                .name(reward.getName())
                .shortDescription(reward.getDescription() != null && reward.getDescription().length() > 100
                        ? reward.getDescription().substring(0, 100) + "..."
                        : reward.getDescription())
                .pointsCost(reward.getPointsCost())
                .monetaryValue(reward.getMonetaryValue())
                .category(reward.getCategory() != null ? reward.getCategory().name() : null)
                .imageUrl(reward.getImageUrl())
                .inStock(reward.isInStock())
                .partnerName(reward.getPartnerName())
                .build();
    }

    private RewardRedemptionResponse toRedemptionResponse(RewardRedemption redemption) {
        return RewardRedemptionResponse.builder()
                .id(redemption.getId())
                .rewardId(redemption.getReward() != null ? redemption.getReward().getId() : null)
                .rewardName(redemption.getReward() != null ? redemption.getReward().getName() : null)
                .pointsSpent(redemption.getPointsSpent())
                .status(redemption.getStatus() != null ? redemption.getStatus().name() : null)
                .redemptionCode(redemption.getRedemptionCode())
                .expiryDate(redemption.getExpiryDate())
                .reviewNote(redemption.getReviewNote())
                .createdAt(redemption.getCreatedAt())
                .build();
    }

    // =========================================================
    // DTOs INTERNES
    // =========================================================

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class RewardSummaryResponse {
        private Long id;
        private String name;
        private String shortDescription;
        private Integer pointsCost;
        private BigDecimal monetaryValue;
        private String category;
        private String imageUrl;
        private boolean inStock;
        private String partnerName;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class RewardDetailResponse {
        private Long id;
        private String name;
        private String description;
        private Integer pointsCost;
        private BigDecimal monetaryValue;
        private String category;
        private String imageUrl;
        private Integer stock;
        private String partnerName;
        private boolean canAfford;
        private boolean isInStock;
        private Integer yourBalance;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class RewardCategoryResponse {
        private String category;
        private List<RewardSummaryResponse> rewards;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class RewardRedemptionResponse {
        private Long id;
        private Long rewardId;
        private String rewardName;
        private Integer pointsSpent;
        private String status;
        private String redemptionCode;
        private LocalDateTime expiryDate;
        private String reviewNote;
        private LocalDateTime createdAt;
    }
}