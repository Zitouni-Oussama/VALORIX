package com.recyclix.backend.service.collector;

import com.recyclix.backend.dto.collection.CollectionResponseDTO;
import com.recyclix.backend.dto.collection.CollectionSummaryDTO;
import com.recyclix.backend.dto.collection_request.CollectionRequestResponseDTO;
import com.recyclix.backend.dto.collection_request.CollectionRequestSummaryDTO;
import com.recyclix.backend.dto.collector_location_history.CollectorLocationHistoryResponseDTO;
import com.recyclix.backend.dto.collector_location_history.CollectorLocationHistoryUpdateDTO;
import com.recyclix.backend.event.CollectionCompletedEvent;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ConflictException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.exception.UnauthorizedException;
import com.recyclix.backend.mapper.CollectionMapper;
import com.recyclix.backend.mapper.CollectionRequestMapper;
import com.recyclix.backend.mapper.CollectorLocationHistoryMapper;
import com.recyclix.backend.model.*;
import com.recyclix.backend.repository.*;
import com.recyclix.backend.service.admin.CancellationMonitoringService;
import com.recyclix.backend.util.SecurityUtils;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import com.recyclix.backend.dto.collection.ValidationCodeResponseDTO;
import com.recyclix.backend.dto.collection_request.ValidationCodeRequestDTO;
import com.recyclix.backend.model.CollectionRequest.CodeStatus;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CollectorCollectionService {

    private final AccountRepository accountRepository;
    private final CollectorRepository collectorRepository;
    private final CollectionRequestRepository collectionRequestRepository;
    private final CollectionRepository collectionRepository;
    private final CollectorLocationHistoryRepository collectorLocationHistoryRepository;
    private final WalletRepository walletRepository;
    private final FactoryDeliveryRepository factoryDeliveryRepository;
    private final CollectionRequestMapper collectionRequestMapper;
    private final CollectionMapper collectionMapper;
    private final CollectorLocationHistoryMapper collectorLocationHistoryMapper;
    private final ClientRepository clientRepository;
    private final PointMovementRepository pointMovementRepository;

    private final ApplicationEventPublisher eventPublisher;

    private final CancellationLogRepository cancellationLogRepository;
    private final CancellationMonitoringService monitoringService;

    @Value("${recyclix.storage.collector-proof-path:uploads/collector/collections/proof}")
    private String collectorProofPath;

    @Value("${recyclix.storage.allowed-extensions}")
    private String allowedExtensions;

    @Value("${recyclix.storage.max-file-size}")
    private long maxFileSize;

    @Transactional(readOnly = true)
    public List<CollectionRequestSummaryDTO> getAvailableRequests() {
        // À adapter selon tes méthodes repository exactes
        return collectionRequestRepository.findAll().stream()
                .filter(r -> r.getStatus() == CollectionRequest.Status.PENDING)
                .filter(r -> r.getCollection() == null)
                .map(collectionRequestMapper::toSummaryDto)
                .toList();
    }

    public CollectionRequestResponseDTO acceptRequest(Long requestId) {
        if (requestId == null) {
            throw new BadRequestException("L'identifiant de la demande est obligatoire.");
        }

        Collector collector = getAuthenticatedCollector();
        CollectionRequest request = collectionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande de collecte introuvable."));

        if (request.getStatus() != CollectionRequest.Status.PENDING) {
            throw new ConflictException("Cette demande n'est plus disponible.");
        }

        request.setStatus(CollectionRequest.Status.ACCEPTED);
        CollectionRequest saved = collectionRequestRepository.save(request);

        return collectionRequestMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<CollectionRequestSummaryDTO> getMyActiveRequests() {
        Collector collector = getAuthenticatedCollector();

        // Hypothèse pragmatique: une mission active = requête ACCEPTED qui a une collection liée à ce collecteur ou pas encore terminée.
        return collectionRequestRepository.findAll().stream()
                .filter(r -> r.getStatus() == CollectionRequest.Status.ACCEPTED
                        || r.getStatus() == CollectionRequest.Status.COLLECTED
                        || r.getStatus() == CollectionRequest.Status.DELIVERED)
                .filter(r -> r.getCollection() == null || (
                        r.getCollection() != null &&
                                r.getCollection().getCollector() != null &&
                                r.getCollection().getCollector().getId().equals(collector.getId())
                ))
                .map(collectionRequestMapper::toSummaryDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CollectionSummaryDTO> getMyCollectionsHistory() {
        Collector collector = getAuthenticatedCollector();

        return collectionRepository.findAll().stream()
                .filter(c -> c.getCollector() != null && c.getCollector().getId().equals(collector.getId()))
                .map(collectionMapper::toSummaryDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public CollectionRequestResponseDTO getAcceptedRequestDetails(Long requestId) {
        if (requestId == null) {
            throw new BadRequestException("L'identifiant de la demande est obligatoire.");
        }

        CollectionRequest request = collectionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable."));

        return collectionRequestMapper.toDto(request);
    }

    @Transactional(readOnly = true)
    public CollectionResponseDTO getMyCollectionDetails(Long collectionId) {
        if (collectionId == null) {
            throw new BadRequestException("L'identifiant de la collecte est obligatoire.");
        }

        Collector collector = getAuthenticatedCollector();

        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Collecte introuvable."));

        if (collection.getCollector() == null || !collection.getCollector().getId().equals(collector.getId())) {
            throw new UnauthorizedException("Cette collecte n'appartient pas au collecteur connecté.");
        }

        return collectionMapper.toDto(collection);
    }

    public CollectionResponseDTO completeCollection(
            Long requestId,
            CompleteCollectionRequest dto,
            MultipartFile collectionProofImage
    ) {
        if (requestId == null) {
        throw new BadRequestException("L'identifiant de la demande est obligatoire.");
    }

    if (dto == null) {
        throw new BadRequestException("Les données de fin de collecte sont obligatoires.");
    }

    Collector collector = getAuthenticatedCollector();
    Account collectorAccount = getAuthenticatedCollectorAccount();

    CollectionRequest request = collectionRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Demande de collecte introuvable."));

    if (request.getStatus() != CollectionRequest.Status.ACCEPTED) {
        throw new BadRequestException("Seule une demande acceptée peut être terminée.");
    }

    if (request.getCollection() != null) {
        throw new ConflictException("Cette demande a déjà une collecte enregistrée.");
    }

    if (request.getClient() == null) {
        throw new ResourceNotFoundException("Aucun client associé à cette demande.");
    }

    BigDecimal estimatedQuantity = request.getEstimatedQuantity();
    if (estimatedQuantity == null || estimatedQuantity.compareTo(BigDecimal.ZERO) <= 0) {
        throw new BadRequestException("La quantité estimée de la demande est invalide.");
    }

    BigDecimal estimatedClientAmount = request.getEstimatedAmount();
    if (estimatedClientAmount == null || estimatedClientAmount.compareTo(BigDecimal.ZERO) < 0) {
        throw new BadRequestException("Le montant estimé client est invalide.");
    }

    BigDecimal estimatedCollectorAmount = request.getCollectorPrice();
    if (estimatedCollectorAmount == null || estimatedCollectorAmount.compareTo(BigDecimal.ZERO) < 0) {
        throw new BadRequestException("Le montant estimé collecteur est invalide.");
    }

    BigDecimal finalQuantity;
    if (dto.getRealQuantity() == null) {
        finalQuantity = estimatedQuantity.setScale(3, RoundingMode.HALF_UP);
    } else {
        if (dto.getRealQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("La quantité réelle doit être supérieure à 0.");
        }

        finalQuantity = dto.getRealQuantity().setScale(3, RoundingMode.HALF_UP);
    }

    BigDecimal citizenUnitPrice = estimatedClientAmount
            .divide(estimatedQuantity, 3, RoundingMode.HALF_UP);

    BigDecimal collectorUnitPrice = estimatedCollectorAmount
            .divide(estimatedQuantity, 3, RoundingMode.HALF_UP);

    BigDecimal finalClientAmount = citizenUnitPrice
            .multiply(finalQuantity)
            .setScale(3, RoundingMode.HALF_UP);

    BigDecimal finalCollectorAmount = collectorUnitPrice
            .multiply(finalQuantity)
            .setScale(3, RoundingMode.HALF_UP);

    if (finalQuantity.compareTo(estimatedQuantity) != 0) {
        request.setEstimatedQuantity(finalQuantity);
        request.setEstimatedAmount(finalClientAmount);
        request.setCollectorPrice(finalCollectorAmount);
        request = collectionRequestRepository.save(request);
    }

    String proofImageUrl = null;
    if (collectionProofImage != null && !collectionProofImage.isEmpty()) {
        proofImageUrl = storeCollectionProof(collectionProofImage, collectorAccount.getId());
    }

    Collection collection = Collection.builder()
            .request(request)
            .collector(collector)
            .realQuantity(finalQuantity)
            .unitPriceFrozen(citizenUnitPrice)
            .collectorUnitPrice(collectorUnitPrice)
            .totalAmount(finalClientAmount)
            .collectionProofImageUrl(proofImageUrl)
            .paymentStatus(Collection.PaymentStatus.PAID)
            .build();

    Collection savedCollection = collectionRepository.save(collection);

        FactoryDelivery factoryDelivery = FactoryDelivery.builder()
                .collection(savedCollection)
                .status(FactoryDelivery.DeliveryStatus.PENDING)
                .deliveryDate(LocalDateTime.now())
                .build();
        factoryDeliveryRepository.save(factoryDelivery);

    if (request.getStatus() == CollectionRequest.Status.ACCEPTED) {
        request.setStatus(CollectionRequest.Status.COLLECTED);
    }

    collectionRequestRepository.save(request);

    Wallet collectorWallet = collectorAccount.getWallet();
    if (collectorWallet == null) {
        collectorWallet = walletRepository.findByAccountId(collectorAccount.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet introuvable pour le collecteur."));
    }

    BigDecimal collectorCurrentBalance = collectorWallet.getBalanceMoney() == null
            ? BigDecimal.ZERO
            : collectorWallet.getBalanceMoney();

    collectorWallet.setBalanceMoney(
            collectorCurrentBalance
                    .add(finalCollectorAmount)
                    .setScale(3, RoundingMode.HALF_UP)
    );

    walletRepository.save(collectorWallet);

    Client client = request.getClient();
    Account clientAccount = client.getAccount();

    if (clientAccount == null) {
        throw new ResourceNotFoundException("Compte client introuvable.");
    }

    Wallet clientWallet = clientAccount.getWallet();
    if (clientWallet == null) {
        clientWallet = walletRepository.findByAccountId(clientAccount.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet introuvable pour le client."));
    }

    // Nouvelle règle :
    // 1 DA = 0.25 point
    int earnedPoints = finalClientAmount
            .multiply(new BigDecimal("0.25"))
            .setScale(0, RoundingMode.DOWN)
            .intValue();

    Integer currentWalletPoints = clientWallet.getBalancePoints() == null
            ? 0
            : clientWallet.getBalancePoints();

    clientWallet.setBalancePoints(currentWalletPoints + earnedPoints);

    // Nouvelle règle :
    // 1 point = 4 DA
    BigDecimal moneyFromPoints = BigDecimal.valueOf(earnedPoints)
            .multiply(new BigDecimal("0.5"))
            .setScale(3, RoundingMode.HALF_UP);

    BigDecimal clientCurrentBalance = clientWallet.getBalanceMoney() == null
            ? BigDecimal.ZERO
            : clientWallet.getBalanceMoney();

    clientWallet.setBalanceMoney(
            clientCurrentBalance
                    .add(moneyFromPoints)
                    .setScale(3, RoundingMode.HALF_UP)
    );

    walletRepository.save(clientWallet);

    client.addPoints(earnedPoints);
    clientRepository.save(client);

    PointMovement pointMovement = PointMovement.builder()
            .account(clientAccount)
            .collection(savedCollection)
            .type(PointMovement.PointMovementType.EARN)
            .pointsAmount(earnedPoints)
            .build();

    pointMovementRepository.save(pointMovement);

    // Après collectionRepository.save(collection)
    eventPublisher.publishEvent(new CollectionCompletedEvent(this, savedCollection));

    return collectionMapper.toDto(savedCollection);
}

    public CollectorLocationHistoryResponseDTO updateMyCurrentLocation(CollectorLocationHistoryUpdateDTO dto) {
        if (dto == null) {
            throw new BadRequestException("Les coordonnées sont obligatoires.");
        }
        if (dto.getLatitude() == null || dto.getLongitude() == null) {
            throw new BadRequestException("Latitude et longitude sont obligatoires.");
        }

        Collector collector = getAuthenticatedCollector();

        // update collector direct
        collector.setCurrentLatitude(dto.getLatitude());
        collector.setCurrentLongitude(dto.getLongitude());
        collectorRepository.save(collector);

        CollectorLocationHistory location = collector.getCollectorLocationHistory();

        if (location == null) {
            location = CollectorLocationHistory.builder()
                    .collector(collector)
                    .latitude(dto.getLatitude())
                    .longitude(dto.getLongitude())
                    .build();
        } else {
            location.setLatitude(dto.getLatitude());
            location.setLongitude(dto.getLongitude());
        }

        CollectorLocationHistory saved = collectorLocationHistoryRepository.save(location);
        return collectorLocationHistoryMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public CollectorLocationHistoryResponseDTO getMyCurrentLocation() {
        Collector collector = getAuthenticatedCollector();

        CollectorLocationHistory location = collector.getCollectorLocationHistory();
        if (location == null) {
            throw new ResourceNotFoundException("Position actuelle introuvable.");
        }

        return collectorLocationHistoryMapper.toDto(location);
    }

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

    private Collector getAuthenticatedCollector() {
        Account account = getAuthenticatedCollectorAccount();

        if (account.getCollector() == null) {
            throw new ResourceNotFoundException("Profil collecteur introuvable.");
        }

        return account.getCollector();
    }

    private String storeCollectionProof(MultipartFile file, Long accountId) {
        validateImage(file);

        try {
            String extension = getFileExtension(file.getOriginalFilename());
            Path uploadDir = Path.of(collectorProofPath).toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);

            String fileName = "collector_collection_" + accountId + "_" + UUID.randomUUID() + "." + extension;
            Path targetPath = uploadDir.resolve(fileName);

            file.transferTo(targetPath.toFile());
            return collectorProofPath.replace("\\", "/") + "/" + fileName;
        } catch (IOException e) {
            throw new BadRequestException("Impossible de sauvegarder la preuve de collecte : " + e.getMessage());
        }
    }

    private void validateImage(MultipartFile file) {
        String extension = getFileExtension(file.getOriginalFilename());

        boolean allowed = List.of(allowedExtensions.toLowerCase().split(","))
                .contains(extension.toLowerCase());

        if (!allowed) {
            throw new BadRequestException("Format d'image non supporté.");
        }

        if (file.getSize() > maxFileSize) {
            throw new BadRequestException("L'image dépasse la taille maximale autorisée.");
        }
    }

    private String getFileExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            throw new BadRequestException("Nom de fichier invalide.");
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex < 0 || lastDotIndex == fileName.length() - 1) {
            throw new BadRequestException("Extension de fichier introuvable.");
        }

        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CompleteCollectionRequest {
        @NotNull(message = "La quantité réelle est obligatoire.")
        private BigDecimal realQuantity;
    }

    // recyclix/backend/service/collector/CollectorCollectionService.java



    // Ajouter cette méthode dans la classe
    @Transactional
    public ValidationCodeResponseDTO validateCollectionWithCode(Long requestId, ValidationCodeRequestDTO codeRequest) {

        // 1. Vérifier les paramètres
        if (requestId == null) throw new BadRequestException("L'identifiant de la demande est obligatoire.");
        if (codeRequest == null || codeRequest.getValidationCode() == null || codeRequest.getValidationCode().isBlank())
            throw new BadRequestException("Le code de validation est obligatoire.");

        // 2. Récupérer le collecteur authentifié
        Collector collector = getAuthenticatedCollector();
        Account collectorAccount = getAuthenticatedCollectorAccount();

        // 3. Récupérer la demande
        CollectionRequest request = collectionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande de collecte introuvable."));

        // 4. Vérifier que la demande est en statut ACCEPTED
        if (request.getStatus() != CollectionRequest.Status.ACCEPTED) {
            throw new BadRequestException("Cette demande n'est pas en cours. Statut actuel : " + request.getStatus());
        }

        // 5. Vérifier que le code correspond
        if (!request.getValidationCode().equals(codeRequest.getValidationCode())) {
            throw new BadRequestException("Code de validation incorrect.");
        }

        // 6. Vérifier que le code n'a pas déjà été utilisé
        if (request.getCodeStatus() == CodeStatus.USED) {
            throw new BadRequestException("Ce code de validation a déjà été utilisé.");
        }

        // 7. Vérifier l'expiration (48h)
        LocalDateTime expiryTime = request.getCodeGeneratedAt().plusHours(48);
        if (LocalDateTime.now().isAfter(expiryTime)) {
            request.setCodeStatus(CodeStatus.EXPIRED);
            collectionRequestRepository.save(request);
            throw new BadRequestException("Ce code de validation a expiré. Veuillez demander un nouveau code.");
        }

        // 8. Vérifier qu'il n'y a pas déjà une collecte associée
        if (request.getCollection() != null) {
            throw new ConflictException("Cette demande a déjà une collecte enregistrée.");
        }

        // 9. Marquer le code comme USED
        request.setCodeStatus(CodeStatus.USED);
        request.setCodeValidatedAt(LocalDateTime.now());
        request.setValidatedByCollectorId(collector.getId());
        collectionRequestRepository.save(request);

        // 10. Déterminer la quantité réelle finalement collectée
        BigDecimal finalQuantity;
        if (codeRequest.getRealQuantity() != null && codeRequest.getRealQuantity().compareTo(BigDecimal.ZERO) > 0) {
            finalQuantity = codeRequest.getRealQuantity();
        } else {
            finalQuantity = request.getEstimatedQuantity(); // valeur par défaut
        }

        // 11. Recalculer les montants à partir de la quantité réelle
        BigDecimal estimatedQuantity = request.getEstimatedQuantity();
        BigDecimal estimatedClientAmount = request.getEstimatedAmount();
        BigDecimal estimatedCollectorAmount = request.getCollectorPrice();

        // Prix unitaires à partir des estimations
        BigDecimal citizenUnitPrice = estimatedClientAmount.divide(estimatedQuantity, 3, RoundingMode.HALF_UP);
        BigDecimal collectorUnitPrice = estimatedCollectorAmount.divide(estimatedQuantity, 3, RoundingMode.HALF_UP);

        BigDecimal finalClientAmount = citizenUnitPrice.multiply(finalQuantity).setScale(3, RoundingMode.HALF_UP);
        BigDecimal finalCollectorAmount = collectorUnitPrice.multiply(finalQuantity).setScale(3, RoundingMode.HALF_UP);

        // Mettre à jour la demande avec les nouvelles valeurs (optionnel mais cohérent)
        request.setEstimatedQuantity(finalQuantity);
        request.setEstimatedAmount(finalClientAmount);
        request.setCollectorPrice(finalCollectorAmount);
        request = collectionRequestRepository.save(request);

        // 12. Créer la collecte
        Collection collection = Collection.builder()
                .request(request)
                .collector(collector)
                .realQuantity(finalQuantity)
                .unitPriceFrozen(citizenUnitPrice)
                .totalAmount(finalClientAmount)
                .collectionProofImageUrl(null) // pas de photo dans ce scénario
                .paymentStatus(Collection.PaymentStatus.PAID)
                .build();
        Collection savedCollection = collectionRepository.save(collection);

        // 13. Créer la livraison d'usine associée
        FactoryDelivery factoryDelivery = FactoryDelivery.builder()
                .collection(savedCollection)
                .status(FactoryDelivery.DeliveryStatus.PENDING)
                .deliveryDate(LocalDateTime.now())
                .build();
        factoryDeliveryRepository.save(factoryDelivery);

        // 14. Mettre à jour le statut de la demande
        request.setStatus(CollectionRequest.Status.COLLECTED);
        collectionRequestRepository.save(request);

        // 15. Gestion des gains du collecteur (argent)
        Wallet collectorWallet = collectorAccount.getWallet();
        if (collectorWallet == null) {
            collectorWallet = walletRepository.findByAccountId(collectorAccount.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Wallet introuvable pour le collecteur."));
        }
        BigDecimal collectorCurrentBalance = collectorWallet.getBalanceMoney() == null ? BigDecimal.ZERO : collectorWallet.getBalanceMoney();
        collectorWallet.setBalanceMoney(collectorCurrentBalance.add(finalCollectorAmount).setScale(3, RoundingMode.HALF_UP));
        walletRepository.save(collectorWallet);

        // 16. Gestion des gains du client (points + argent)
        Client client = request.getClient();
        Account clientAccount = client.getAccount();
        if (clientAccount == null) throw new ResourceNotFoundException("Compte client introuvable.");

        Wallet clientWallet = clientAccount.getWallet();
        if (clientWallet == null) {
            clientWallet = walletRepository.findByAccountId(clientAccount.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Wallet introuvable pour le client."));
        }

        // Points : 1 DA client = 0.25 point => points = montantClient * 0.25
        int earnedPoints = finalClientAmount.multiply(new BigDecimal("0.25")).setScale(0, RoundingMode.DOWN).intValue();
        clientWallet.setBalancePoints((clientWallet.getBalancePoints() == null ? 0 : clientWallet.getBalancePoints()) + earnedPoints);
        // Argent : 1 point = 0.5 DA => argent = points * 0.5
        BigDecimal moneyFromPoints = BigDecimal.valueOf(earnedPoints).multiply(new BigDecimal("0.5")).setScale(3, RoundingMode.HALF_UP);
        clientWallet.setBalanceMoney((clientWallet.getBalanceMoney() == null ? BigDecimal.ZERO : clientWallet.getBalanceMoney()).add(moneyFromPoints));
        walletRepository.save(clientWallet);

        client.addPoints(earnedPoints);
        clientRepository.save(client);

        // 17. Enregistrer le mouvement de points
        PointMovement pointMovement = PointMovement.builder()
                .account(clientAccount)
                .collection(savedCollection)
                .type(PointMovement.PointMovementType.EARN)
                .pointsAmount(earnedPoints)
                .build();
        pointMovementRepository.save(pointMovement);

        // 18. Déclencher l’événement pour les défis
        eventPublisher.publishEvent(new CollectionCompletedEvent(this, savedCollection));

        // 19. Retourner la réponse
        return ValidationCodeResponseDTO.builder()
                .success(true)
                .message("Collecte validée avec succès ! Les points ont été crédités.")
                .collectionId(savedCollection.getId())
                .requestStatus(request.getStatus().name())
                .codeStatus(request.getCodeStatus().name())
                .build();
    }

    /**
     * Rafraîchir le code de validation (si le client l'a perdu)
     */
    @Transactional
    public CollectionRequestResponseDTO refreshValidationCode(Long requestId) {

        Client client = getAuthenticatedClient();

        CollectionRequest request = collectionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande de collecte introuvable."));

        // Vérifier que le client est bien le propriétaire
        if (request.getClient() == null || !request.getClient().getId().equals(client.getId())) {
            throw new UnauthorizedException("Cette demande ne vous appartient pas.");
        }

        // Vérifier que la demande est encore en attente ou acceptée
        if (request.getStatus() != CollectionRequest.Status.PENDING &&
                request.getStatus() != CollectionRequest.Status.ACCEPTED) {
            throw new BadRequestException("Impossible de générer un nouveau code pour cette demande.");
        }

        // Vérifier que le code n'a pas déjà été utilisé
        if (request.getCodeStatus() == CodeStatus.USED) {
            throw new BadRequestException("Ce code a déjà été utilisé. La collecte est terminée.");
        }

        // Générer un nouveau code
        String newCode = generateNewValidationCode();
        request.setValidationCode(newCode);
        request.setCodeGeneratedAt(LocalDateTime.now());
        request.setCodeStatus(CodeStatus.PENDING);

        CollectionRequest saved = collectionRequestRepository.save(request);

        return collectionRequestMapper.toDto(saved);
    }

    private String generateNewValidationCode() {
        LocalDateTime now = LocalDateTime.now();
        String datePart = now.format(DateTimeFormatter.ofPattern("yyMMdd"));
        String randomPart = generateRandomCode(4);
        return String.format("RX-%s-%s", datePart, randomPart);
    }

    private String generateRandomCode(int length) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // Méthode helper pour récupérer le client authentifié (si pas déjà présente)
    private Client getAuthenticatedClient() {
        String email = SecurityUtils.getEmail()
                .orElseThrow(() -> new UnauthorizedException("Utilisateur non authentifié."));

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable."));

        if (account.getRoleType() != Account.RoleType.CLIENT) {
            throw new UnauthorizedException("Accès réservé au client.");
        }

        return clientRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profil client introuvable."));
    }

    // Dans CollectorCollectionService.java

    @Transactional
    public CollectionRequestResponseDTO cancelAcceptance(Long requestId) {
        CollectionRequest request = collectionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable"));

        Collector collector = getAuthenticatedCollector();

        if (request.getCollection() != null) {
            throw new BadRequestException("Vous ne pouvez pas annuler une acceptation si la collecte a déjà commencé.");
        }
        if (request.getStatus() != CollectionRequest.Status.ACCEPTED) {
            throw new BadRequestException("Seule une demande acceptée peut être annulée.");
        }

        // --- Enregistrement de l'annulation ---
        Account collectorAccount = getAuthenticatedCollectorAccount();
        CancellationLog log = CancellationLog.builder()
                .accountId(collectorAccount.getId())
                .requestId(requestId)
                .roleAtTime(collectorAccount.getRoleType())
                .reason("Annulation d'acceptation par collecteur")
                .build();
        cancellationLogRepository.save(log);

        // --- Surveillance des seuils ---
        monitoringService.checkAccountCancellations(collectorAccount.getId());

        // --- Remise en PENDING ---
        request.setStatus(CollectionRequest.Status.PENDING);
        CollectionRequest saved = collectionRequestRepository.save(request);

        return collectionRequestMapper.toDto(saved);
    }
}