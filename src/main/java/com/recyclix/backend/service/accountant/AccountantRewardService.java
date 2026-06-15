package com.recyclix.backend.service.accountant;

import com.recyclix.backend.controller.accountant.AccountantRewardController;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.exception.UnauthorizedException;
import com.recyclix.backend.model.FactoryUser;
import com.recyclix.backend.model.Reward;
import com.recyclix.backend.model.Reward.RewardCategory;
import com.recyclix.backend.model.RewardRedemption;
import com.recyclix.backend.model.RewardRedemption.RedemptionStatus;
import com.recyclix.backend.model.Wallet;
import com.recyclix.backend.repository.FactoryUserRepository;
import com.recyclix.backend.repository.RewardRedemptionRepository;
import com.recyclix.backend.repository.RewardRepository;
import com.recyclix.backend.repository.WalletRepository;
import com.recyclix.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountantRewardService {

    private final RewardRedemptionRepository redemptionRepository;
    private final RewardRepository rewardRepository;
    private final FactoryUserRepository factoryUserRepository;
    private final WalletRepository walletRepository;

    // =========================================================
    // CRÉER UNE RÉCOMPENSE
    // =========================================================

    public RewardResponse createReward(AccountantRewardController.CreateRewardRequest request) {
        FactoryUser creator = getAuthenticatedFactoryUser();

        RewardCategory category;
        try {
            category = RewardCategory.valueOf(request.getCategory().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Catégorie invalide. Valeurs autorisées : " +
                    String.join(", ", java.util.Arrays.stream(RewardCategory.values())
                            .map(Enum::name)
                            .toArray(String[]::new)));
        }

        Reward reward = Reward.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .pointsCost(request.getPointsCost())
                .monetaryValue(request.getMonetaryValue())
                .category(category)
                .imageUrl(request.getImageUrl())
                .stock(request.getStock() != null ? request.getStock() : -1)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .partnerName(request.getPartnerName())
                .validityDays(request.getValidityDays())
                .deliveryInstructions(request.getDeliveryInstructions())
                .createdBy(creator)
                .build();

        Reward saved = rewardRepository.save(reward);
        return toRewardResponse(saved);
    }

    // =========================================================
    // METTRE À JOUR UNE RÉCOMPENSE
    // =========================================================

    public RewardResponse updateReward(Long rewardId, AccountantRewardController.UpdateRewardRequest request) {
        if (rewardId == null) {
            throw new BadRequestException("L'identifiant de la récompense est obligatoire.");
        }

        Reward reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> new ResourceNotFoundException("Récompense introuvable."));

        if (request.getName() != null && !request.getName().isBlank()) {
            reward.setName(request.getName().trim());
        }

        if (request.getDescription() != null) {
            reward.setDescription(request.getDescription());
        }

        if (request.getPointsCost() != null) {
            if (request.getPointsCost() < 1) {
                throw new BadRequestException("Le coût en points doit être d'au moins 1.");
            }
            reward.setPointsCost(request.getPointsCost());
        }

        if (request.getMonetaryValue() != null) {
            reward.setMonetaryValue(request.getMonetaryValue());
        }

        if (request.getCategory() != null) {
            try {
                reward.setCategory(RewardCategory.valueOf(request.getCategory().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Catégorie invalide.");
            }
        }

        if (request.getImageUrl() != null) {
            reward.setImageUrl(request.getImageUrl());
        }

        if (request.getStock() != null) {
            reward.setStock(request.getStock());
        }

        if (request.getIsActive() != null) {
            reward.setIsActive(request.getIsActive());
        }

        if (request.getPartnerName() != null) {
            reward.setPartnerName(request.getPartnerName());
        }

        if (request.getValidityDays() != null) {
            reward.setValidityDays(request.getValidityDays());
        }

        if (request.getDeliveryInstructions() != null) {
            reward.setDeliveryInstructions(request.getDeliveryInstructions());
        }

        Reward saved = rewardRepository.save(reward);
        return toRewardResponse(saved);
    }

    // =========================================================
    // ACTIVER / DÉSACTIVER
    // =========================================================

    public RewardResponse toggleReward(Long rewardId) {
        if (rewardId == null) {
            throw new BadRequestException("L'identifiant de la récompense est obligatoire.");
        }

        Reward reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> new ResourceNotFoundException("Récompense introuvable."));

        reward.setIsActive(!Boolean.TRUE.equals(reward.getIsActive()));
        Reward saved = rewardRepository.save(reward);

        return toRewardResponse(saved);
    }

    // =========================================================
    // SUPPRIMER
    // =========================================================

    public void deleteReward(Long rewardId) {
        if (rewardId == null) {
            throw new BadRequestException("L'identifiant de la récompense est obligatoire.");
        }

        Reward reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> new ResourceNotFoundException("Récompense introuvable."));

        rewardRepository.delete(reward);
    }

    // =========================================================
    // LISTER TOUTES LES RÉCOMPENSES
    // =========================================================

    @Transactional(readOnly = true)
    public List<RewardResponse> getAllRewards() {
        getAuthenticatedFactoryUser();

        return rewardRepository.findAll().stream()
                .map(this::toRewardResponse)
                .toList();
    }

    // =========================================================
    // GESTION DES DEMANDES (EXISTANT)
    // =========================================================

    @Transactional(readOnly = true)
    public List<RewardRedemptionDetailResponse> getPendingRedemptions() {
        getAuthenticatedFactoryUser();

        List<RewardRedemption> pendingList = redemptionRepository
                .findPendingWithDetails(RedemptionStatus.PENDING);

        return pendingList.stream()
                .map(this::toDetailResponse)
                .toList();
    }

    public RewardRedemptionDetailResponse approveRedemption(
            Long redemptionId,
            AccountantRewardController.ReviewRequest request
    ) {
        if (redemptionId == null) {
            throw new BadRequestException("L'identifiant de l'échange est obligatoire.");
        }

        FactoryUser reviewer = getAuthenticatedFactoryUser();

        RewardRedemption redemption = redemptionRepository.findById(redemptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande d'échange introuvable."));

        if (redemption.getStatus() != RedemptionStatus.PENDING) {
            throw new BadRequestException("Seule une demande en attente peut être approuvée.");
        }

        String note = (request != null && request.getNote() != null)
                ? request.getNote()
                : "Approuvé par le comptable.";

        redemption.approve(reviewer, note);
        RewardRedemption saved = redemptionRepository.save(redemption);

        return toDetailResponse(saved);
    }

    public RewardRedemptionDetailResponse rejectRedemption(
            Long redemptionId,
            AccountantRewardController.ReviewRequest request
    ) {
        if (redemptionId == null) {
            throw new BadRequestException("L'identifiant de l'échange est obligatoire.");
        }

        if (request == null || request.getNote() == null || request.getNote().isBlank()) {
            throw new BadRequestException("La raison du rejet est obligatoire.");
        }

        FactoryUser reviewer = getAuthenticatedFactoryUser();

        RewardRedemption redemption = redemptionRepository.findById(redemptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande d'échange introuvable."));

        if (redemption.getStatus() != RedemptionStatus.PENDING) {
            throw new BadRequestException("Seule une demande en attente peut être rejetée.");
        }

        // Rembourser les points
        Wallet wallet = redemption.getAccount().getWallet();
        if (wallet != null) {
            wallet.setBalancePoints(
                    wallet.getBalancePoints() + redemption.getPointsSpent()
            );
            walletRepository.save(wallet);
        }

        // Remettre le stock
        Reward reward = redemption.getReward();
        if (reward != null) {
            reward.incrementStock();
        }

        redemption.reject(reviewer, request.getNote());
        RewardRedemption saved = redemptionRepository.save(redemption);

        return toDetailResponse(saved);
    }

    public RewardRedemptionDetailResponse markAsDelivered(Long redemptionId , AccountantRewardController.ReviewRequest request) {
        if (redemptionId == null) {
            throw new BadRequestException("L'identifiant de l'échange est obligatoire.");
        }

        getAuthenticatedFactoryUser();

        RewardRedemption redemption = redemptionRepository.findById(redemptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande d'échange introuvable."));

        if (redemption.getStatus() != RedemptionStatus.APPROVED) {
            throw new BadRequestException("Seule une demande approuvée peut être marquée comme livrée.");
        }
        if (request != null && request.getNote() != null && !request.getNote().isBlank()) {
            redemption.setReviewNote(request.getNote());
        }

        redemption.markAsDelivered();
        RewardRedemption saved = redemptionRepository.save(redemption);
        return toDetailResponse(saved);
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private FactoryUser getAuthenticatedFactoryUser() {
        Long accountId = SecurityUtils.getAccountId()
                .orElseThrow(() -> new UnauthorizedException("Utilisateur non authentifié."));

        return factoryUserRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Employé usine introuvable."));
    }

    // ---- Mappers ----

    private RewardResponse toRewardResponse(Reward reward) {
        return RewardResponse.builder()
                .id(reward.getId())
                .name(reward.getName())
                .description(reward.getDescription())
                .pointsCost(reward.getPointsCost())
                .monetaryValue(reward.getMonetaryValue())
                .category(reward.getCategory() != null ? reward.getCategory().name() : null)
                .imageUrl(reward.getImageUrl())
                .stock(reward.getStock())
                .isActive(reward.getIsActive())
                .partnerName(reward.getPartnerName())
                .validityDays(reward.getValidityDays())
                .deliveryInstructions(reward.getDeliveryInstructions())
                .createdAt(reward.getCreatedAt())
                .updatedAt(reward.getUpdatedAt())
                .isInStock(reward.isInStock())
                .build();
    }

    private RewardRedemptionDetailResponse toDetailResponse(RewardRedemption rr) {
        return RewardRedemptionDetailResponse.builder()
                .id(rr.getId())
                .accountEmail(rr.getAccount() != null ? rr.getAccount().getEmail() : null)
                .rewardName(rr.getReward() != null ? rr.getReward().getName() : null)
                .rewardCategory(rr.getReward() != null && rr.getReward().getCategory() != null
                        ? rr.getReward().getCategory().name() : null)
                .pointsSpent(rr.getPointsSpent())
                .monetaryValue(rr.getReward() != null ? rr.getReward().getMonetaryValue() : null)
                .status(rr.getStatus() != null ? rr.getStatus().name() : null)
                .redemptionCode(rr.getRedemptionCode())
                .additionalInfo(rr.getAdditionalInfo())
                .reviewNote(rr.getReviewNote())
                .expiryDate(rr.getExpiryDate())
                .createdAt(rr.getCreatedAt())
                .updatedAt(rr.getUpdatedAt())
                .build();
    }

    // =========================================================
    // DTOs INTERNES
    // =========================================================

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class RewardResponse {
        private Long id;
        private String name;
        private String description;
        private Integer pointsCost;
        private java.math.BigDecimal monetaryValue;
        private String category;
        private String imageUrl;
        private Integer stock;
        private Boolean isActive;
        private String partnerName;
        private Integer validityDays;
        private String deliveryInstructions;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime updatedAt;
        private boolean isInStock;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class RewardRedemptionDetailResponse {
        private Long id;
        private String accountEmail;
        private String rewardName;
        private String rewardCategory;
        private Integer pointsSpent;
        private java.math.BigDecimal monetaryValue;
        private String status;
        private String redemptionCode;
        private String additionalInfo;
        private String reviewNote;
        private java.time.LocalDateTime expiryDate;
        private java.time.LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}