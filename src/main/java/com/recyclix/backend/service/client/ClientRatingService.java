package com.recyclix.backend.service.client;

import com.recyclix.backend.dto.collection.CollectionResponseDTO;
import com.recyclix.backend.dto.rating.RatingRequestDTO;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.exception.UnauthorizedException;
import com.recyclix.backend.mapper.CollectionMapper;
import com.recyclix.backend.model.Account;
import com.recyclix.backend.model.Collection;
import com.recyclix.backend.model.CollectionRequest;
import com.recyclix.backend.repository.AccountRepository;
import com.recyclix.backend.repository.CollectionRepository;
import com.recyclix.backend.repository.CollectorRepository;
import com.recyclix.backend.service.collector.CollectorRatingService;
import com.recyclix.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientRatingService {

    private final AccountRepository accountRepository;
    private final CollectionRepository collectionRepository;
    private final CollectorRepository collectorRepository;
    private final CollectionMapper collectionMapper;
    private final CollectorRatingService collectorRatingService;

    /**
     * NOTE UNE COLLECTE
     * Conditions :
     * - Client authentifié et propriétaire de la demande
     * - Statut de la demande = COLLECTED ou DELIVERED (collecte terminée)
     * - La collecte n'a pas encore été notée
     */
    public CollectionResponseDTO rateCollection(Long collectionId, RatingRequestDTO request) {
        // 1. Vérifier l'utilisateur connecté
        Account account = getAuthenticatedClientAccount();

        // 2. Récupérer la collecte
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Collecte introuvable."));

        // 3. Vérifier que le client est bien le propriétaire de la demande
        if (collection.getRequest() == null ||
                collection.getRequest().getClient() == null ||
                !collection.getRequest().getClient().getAccount().getId().equals(account.getId())) {
            throw new UnauthorizedException("Vous n'êtes pas autorisé à noter cette collecte.");
        }

        // 4. Vérifier que la collecte est terminée (COLLECTED ou DELIVERED)
        CollectionRequest.Status requestStatus = collection.getRequest().getStatus();
        if (requestStatus != CollectionRequest.Status.COLLECTED &&
                requestStatus != CollectionRequest.Status.DELIVERED) {
            throw new BadRequestException(
                    "Vous ne pouvez noter qu'une collecte terminée (statut COLLECTED ou DELIVERED). " +
                            "Statut actuel : " + requestStatus
            );
        }

        // 5. Vérifier que la collecte n'a pas déjà été notée
        if (collection.getRating() != null) {
            throw new BadRequestException("Cette collecte a déjà été notée.");
        }

        // 6. Enregistrer la note et le commentaire
        collection.setRating(request.getRating());
        collection.setFeedbackComment(request.getComment());
        collection.setRatedAt(LocalDateTime.now());

        Collection savedCollection = collectionRepository.save(collection);

        // 7. Mettre à jour la moyenne du collecteur
        Long collectorId = collection.getCollector().getId();
        collectorRatingService.updateCollectorAverageRating(collectorId);

        return collectionMapper.toDto(savedCollection);
    }

    /**
     * RÉCUPÉRER LA NOTE D'UNE COLLECTE
     */
    public RatingRequestDTO getRatingForCollection(Long collectionId) {
        Account account = getAuthenticatedClientAccount();

        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Collecte introuvable."));

        if (collection.getRequest() == null ||
                collection.getRequest().getClient() == null ||
                !collection.getRequest().getClient().getAccount().getId().equals(account.getId())) {
            throw new UnauthorizedException("Vous n'êtes pas autorisé à voir cette note.");
        }

        if (collection.getRating() == null) {
            throw new ResourceNotFoundException("Cette collecte n'a pas encore été notée.");
        }

        return RatingRequestDTO.builder()
                .rating(collection.getRating())
                .comment(collection.getFeedbackComment())
                .build();
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
}