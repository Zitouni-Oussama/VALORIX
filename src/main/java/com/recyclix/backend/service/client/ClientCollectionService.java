package com.recyclix.backend.service.client;

import com.recyclix.backend.dto.ai_classification.AIClassificationResponseDTO;
import com.recyclix.backend.dto.collection.CollectionResponseDTO;
import com.recyclix.backend.dto.collection_request.CollectionRequestRequestDTO;
import com.recyclix.backend.dto.collection_request.CollectionRequestResponseDTO;
import com.recyclix.backend.dto.collection_request.CollectionRequestSummaryDTO;
import com.recyclix.backend.dto.collector_location_history.CollectorLocationHistoryResponseDTO;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.exception.UnauthorizedException;
import com.recyclix.backend.mapper.AIClassificationMapper;
import com.recyclix.backend.mapper.CollectionMapper;
import com.recyclix.backend.mapper.CollectionRequestMapper;
import com.recyclix.backend.mapper.CollectorLocationHistoryMapper;
import com.recyclix.backend.model.*;
import com.recyclix.backend.repository.*;
import com.recyclix.backend.service.admin.CancellationMonitoringService;
import com.recyclix.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientCollectionService {

    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;
    private final MaterialRepository materialRepository;
    private final CollectionRequestRepository collectionRequestRepository;
    private final CollectionRepository collectionRepository;
    private final AIClassificationRepository aiClassificationRepository;
    private final CollectorLocationHistoryRepository collectorLocationHistoryRepository;

    private final CollectionRequestMapper collectionRequestMapper;
    private final CollectionMapper collectionMapper;
    private final AIClassificationMapper aiClassificationMapper;
    private final CollectorLocationHistoryMapper collectorLocationHistoryMapper;

    private final CancellationLogRepository cancellationLogRepository;
    private final CancellationMonitoringService monitoringService;

    @Value("${recyclix.storage.collection-request-image-path:uploads/client/collection-request}")
    private String collectionRequestImagePath;

    @Value("${recyclix.storage.allowed-extensions}")
    private String allowedExtensions;

    @Value("${recyclix.storage.max-file-size}")
    private long maxFileSize;

    public CollectionRequestResponseDTO createRequest(
            CollectionRequestRequestDTO dto,
            MultipartFile wasteImage
    ) {
        Client client = getAuthenticatedClient();

        validateCreateRequest(dto);

        CollectionRequest entity = collectionRequestMapper.toEntity(dto);

        entity.setClient(client);

        if (dto.getMaterialId() != null) {
            Material material = materialRepository.findById(dto.getMaterialId())
                    .orElseThrow(() -> new ResourceNotFoundException("Matériau introuvable."));
            entity.setMaterial(material);
        } else {
            entity.setMaterial(null);
        }

        entity.setStatus(CollectionRequest.Status.PENDING);

        if (wasteImage != null && !wasteImage.isEmpty()) {
            String imageUrl = storeWasteImage(wasteImage, client.getId());
            entity.setWasteImageUrl(imageUrl);
        }

        CollectionRequest saved = collectionRequestRepository.save(entity);
        return collectionRequestMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<CollectionRequestSummaryDTO> getMyRequests() {
        Client client = getAuthenticatedClient();

        return collectionRequestRepository.findAllByClientIdOrderByCreatedAtDesc(client.getId())
                .stream()
                .map(collectionRequestMapper::toSummaryDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public CollectionRequestResponseDTO getMyRequestById(Long requestId) {
        CollectionRequest request = getOwnedRequest(requestId);
        return collectionRequestMapper.toDto(request);
    }

    // Dans ClientCollectionService.cancelRequest
    @Transactional
    public CollectionRequestResponseDTO cancelRequest(Long requestId) {
        CollectionRequest request = getOwnedRequest(requestId);

        if (request.getStatus() != CollectionRequest.Status.PENDING && request.getStatus() != CollectionRequest.Status.ACCEPTED) {
            throw new BadRequestException("Seules les demandes en attente ou accepter peuvent être annulées.");
        }

        if (request.getCollection() != null) {
            throw new BadRequestException("Impossible d'annuler une demande déjà prise en charge.");
        }

        // --- ENREGISTREMENT DE L'ANNULATION (NOUVEAU) ---
        Account account = getAuthenticatedClientAccount();
        CancellationLog log = CancellationLog.builder()
                .accountId(account.getId())
                .requestId(requestId)
                .roleAtTime(account.getRoleType())
                .reason("Annulation par le client (statut PENDING)")
                .build();
        cancellationLogRepository.save(log);

        // --- Surveillance des seuils ---
        monitoringService.checkAccountCancellations(account.getId());

        request.setStatus(CollectionRequest.Status.CANCELLED);
        CollectionRequest saved = collectionRequestRepository.save(request);

        return collectionRequestMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public AIClassificationResponseDTO getAiResultForRequest(Long requestId) {
        CollectionRequest request = getOwnedRequest(requestId);

        AIClassification ai = aiClassificationRepository.findByRequestId(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Aucun résultat IA trouvé pour cette demande."));

        return aiClassificationMapper.toDto(ai);
    }

    @Transactional(readOnly = true)
    public CollectionResponseDTO getCollectionForRequest(Long requestId) {
        CollectionRequest request = getOwnedRequest(requestId);

        Collection collection = collectionRepository.findByRequestId(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Aucune collecte trouvée pour cette demande."));

        return collectionMapper.toDto(collection);
    }

    @Transactional(readOnly = true)
    public CollectorLocationHistoryResponseDTO getCollectorLocationForRequest(Long requestId) {
        CollectionRequest request = getOwnedRequest(requestId);

        Collection collection = collectionRepository.findByRequestId(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Aucune collecte trouvée pour cette demande."));

        Collector collector = collection.getCollector();
        if (collector == null) {
            throw new ResourceNotFoundException("Aucun collecteur associé à cette collecte.");
        }

        if (request.getStatus() != CollectionRequest.Status.ACCEPTED) {
            throw new BadRequestException("La localisation du collecteur n'est disponible que pour une collecte acceptée ou en cours.");
        }

        CollectorLocationHistory location =
                collectorLocationHistoryRepository.findTopByCollectorIdOrderByRecordedAtDesc(collector.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Aucune position disponible pour ce collecteur."));

        return collectorLocationHistoryMapper.toDto(location);
    }

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

    public CollectionRequest getOwnedRequest(Long requestId) {
        if (requestId == null) {
            throw new BadRequestException("L'identifiant de la demande est obligatoire.");
        }

        Client client = getAuthenticatedClient();

        CollectionRequest request = collectionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande de collecte introuvable."));

        if (request.getClient() == null || !request.getClient().getId().equals(client.getId())) {
            throw new UnauthorizedException("Cette demande n'appartient pas au client connecté.");
        }

        return request;
    }

    private void validateCreateRequest(CollectionRequestRequestDTO dto) {
        if (dto == null) {
            throw new BadRequestException("Les données de la demande sont obligatoires.");
        }

        if (dto.getEstimatedQuantity() == null || dto.getEstimatedQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("La quantité estimée doit être supérieure à 0.");
        }

        if (dto.getEstimatedAmount() == null || dto.getEstimatedAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Le montant estimé doit être supérieur ou égal à 0.");
        }

        if (dto.getCollectorPrice() == null || dto.getCollectorPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Le prix collecteur doit être supérieur ou égal à 0.");
        }

        if (dto.getLatitude() == null || dto.getLongitude() == null) {
            throw new BadRequestException("La latitude et la longitude sont obligatoires.");
        }
    }

    private String storeWasteImage(MultipartFile file, Long clientId) {
        validateImage(file);

        try {
            String extension = getFileExtension(file.getOriginalFilename());

            Path uploadDir = Path.of(collectionRequestImagePath).toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);

            String fileName = "collection_client_" + clientId + "_" + UUID.randomUUID() + "." + extension;
            Path targetPath = uploadDir.resolve(fileName);

            file.transferTo(targetPath.toFile());

            return collectionRequestImagePath.replace("\\", "/") + "/" + fileName;
        } catch (IOException e) {
            e.printStackTrace();
            throw new BadRequestException("Impossible de sauvegarder l'image des déchets : " + e.getMessage());
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Le fichier image est obligatoire.");
        }

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


    // Dans ClientCollectionService.java, ajoutez cette méthode

    @Transactional
    public CollectionRequestResponseDTO refreshValidationCode(Long requestId) {
        // 1. Vérifier que la demande appartient au client
        CollectionRequest request = getOwnedRequest(requestId);

        // 2. Vérifier que la demande est dans un état valide pour générer un nouveau code
        if (request.getStatus() != CollectionRequest.Status.PENDING &&
                request.getStatus() != CollectionRequest.Status.ACCEPTED) {
            throw new BadRequestException("Impossible de générer un nouveau code pour cette demande. Statut actuel : " + request.getStatus());
        }

        // 3. Vérifier que le code n'a pas déjà été utilisé
        if (request.getCodeStatus() == CollectionRequest.CodeStatus.USED) {
            throw new BadRequestException("Ce code a déjà été utilisé. La collecte est terminée.");
        }

        // 4. Générer un nouveau code
        String newCode = generateNewValidationCode();
        request.setValidationCode(newCode);
        request.setCodeGeneratedAt(LocalDateTime.now());
        request.setCodeStatus(CollectionRequest.CodeStatus.PENDING);

        CollectionRequest saved = collectionRequestRepository.save(request);

        return collectionRequestMapper.toDto(saved);
    }

    // Ajoutez aussi cette méthode helper si elle n'existe pas déjà
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

    // Dans ClientCollectionService.java

    @Transactional
    public CollectionRequestResponseDTO cancelAfterAccept(Long requestId) {
        CollectionRequest request = getOwnedRequest(requestId);

        if (request.getStatus() != CollectionRequest.Status.ACCEPTED) {
            throw new BadRequestException("Seule une demande acceptée peut être annulée.");
        }
        if (request.getCollection() != null) {
            throw new BadRequestException("Impossible d'annuler une demande déjà collectée.");
        }

        // --- Enregistrement de l'annulation ---
        Account account = getAuthenticatedClientAccount(); // à créer si pas déjà présente
        CancellationLog log = CancellationLog.builder()
                .accountId(account.getId())
                .requestId(requestId)
                .roleAtTime(account.getRoleType())
                .reason("Annulation après acceptation par client")
                .build();
        cancellationLogRepository.save(log);

        // --- Surveillance des seuils ---
        monitoringService.checkAccountCancellations(account.getId());

        // --- Mise à jour du statut ---
        request.setStatus(CollectionRequest.Status.PENDING);
        CollectionRequest saved = collectionRequestRepository.save(request);

        return collectionRequestMapper.toDto(saved);
    }

    // Méthode utilitaire pour récupérer le compte client (si pas déjà présente)
    private Account getAuthenticatedClientAccount() {
        String email = SecurityUtils.getEmail()
                .orElseThrow(() -> new UnauthorizedException("Utilisateur non authentifié."));
        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable."));
    }
}