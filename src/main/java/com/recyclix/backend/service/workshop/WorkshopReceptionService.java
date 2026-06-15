package com.recyclix.backend.service.workshop;

import com.recyclix.backend.dto.workshop.CollectionFullDetailsDTO;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ConflictException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.exception.UnauthorizedException;
import com.recyclix.backend.model.*;
import com.recyclix.backend.repository.*;
import com.recyclix.backend.util.SecurityUtils;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkshopReceptionService {

    private final AccountRepository accountRepository;
    private final FactoryUserRepository factoryUserRepository;
    private final CollectionRepository collectionRepository;
    private final FactoryDeliveryRepository factoryDeliveryRepository;
    private final FactoryValidationRepository factoryValidationRepository;
    private final MaterialStockRepository materialStockRepository;

    @Transactional(readOnly = true)
    public List<WorkshopReceptionSummaryResponse> getAllDeliveries() {
        return factoryDeliveryRepository.findAll().stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkshopReceptionSummaryResponse> getPendingDeliveries() {
        return factoryDeliveryRepository.findAll().stream()
                .filter(d -> d.getStatus() == FactoryDelivery.DeliveryStatus.PENDING)
                .map(this::toSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkshopReceptionDetailResponse getDeliveryById(Long deliveryId) {
        if (deliveryId == null) {
            throw new BadRequestException("L'identifiant de la livraison est obligatoire.");
        }

        FactoryDelivery delivery = factoryDeliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Livraison introuvable."));

        return toDetailResponse(delivery);
    }

    public WorkshopReceptionDetailResponse receiveCollection(Long collectionId, ReceiveCollectionRequest request) {
        if (collectionId == null) {
            throw new BadRequestException("L'identifiant de la collecte est obligatoire.");
        }

        if (request == null) {
            throw new BadRequestException("Les données de réception sont obligatoires.");
        }

        FactoryUser currentUser = getAuthenticatedFactoryUser();

        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Collecte introuvable."));

        if (collection.getFactoryDelivery() != null) {
            throw new ConflictException("Cette collecte a déjà une livraison enregistrée.");
        }

        if (collection.getRequest() == null) {
            throw new ResourceNotFoundException("Aucune demande liée à cette collecte.");
        }

        if (collection.getRequest().getStatus() != CollectionRequest.Status.COLLECTED) {
            throw new BadRequestException("Seule une collecte terminée par le collecteur peut être reçue à l'usine.");
        }

        // Récupérer l’usine associée au FactoryUser (peut être null si non assignée)
        RecyclingCenter userRecyclingCenter = currentUser.getRecyclingCenter();

        FactoryDelivery delivery = FactoryDelivery.builder()
                .collection(collection)
                .status(FactoryDelivery.DeliveryStatus.PENDING)
                .deliveryDate(request.getDeliveryDate() != null ? request.getDeliveryDate() : LocalDateTime.now())
                .recyclingCenter(userRecyclingCenter)   // ← affectation automatique
                .build();

        FactoryDelivery savedDelivery = factoryDeliveryRepository.save(delivery);
        return toDetailResponse(savedDelivery);
    }

//    @Transactional
//    public WorkshopReceptionDetailResponse validateDelivery(Long deliveryId, ValidateDeliveryRequest request) {
//        if (deliveryId == null) {
//            throw new BadRequestException("L'identifiant de la livraison est obligatoire.");
//        }
//
//        if (request == null) {
//            throw new BadRequestException("Les données de validation sont obligatoires.");
//        }
//
//        if (request.getValidatedWeight() == null) {
//            throw new BadRequestException("Le poids validé est obligatoire.");
//        }
//
//        FactoryUser currentUser = getAuthenticatedFactoryUser();
//
//        FactoryDelivery delivery = factoryDeliveryRepository.findById(deliveryId)
//                .orElseThrow(() -> new ResourceNotFoundException("Livraison introuvable."));
//
//        Collection collection = delivery.getCollection();
//        if (collection == null) {
//            throw new ResourceNotFoundException("Aucune collecte liée à cette livraison.");
//        }
//
//        CollectionRequest collectionRequest = collection.getRequest();
//        if (collectionRequest == null) {
//            throw new ResourceNotFoundException("Aucune demande liée à cette collecte.");
//        }
//
//        // La demande doit rester COLLECTED côté client
//        if (collectionRequest.getStatus() != CollectionRequest.Status.COLLECTED) {
//            throw new BadRequestException("Seule une collecte au statut COLLECTED peut être validée par l'usine.");
//        }
//
//        BigDecimal declaredWeight = collection.getRealQuantity();
//        if (declaredWeight == null) {
//            throw new BadRequestException("Le poids déclaré de la collecte est introuvable.");
//        }
//
//        FactoryValidation validation = delivery.getValidation();
//
//        RecyclingCenter userRecyclingCenter = currentUser.getRecyclingCenter();
//
//        if (validation == null) {
//            validation = FactoryValidation.builder()
//                    .delivery(delivery)
//                    .validatedBy(currentUser)
//                    .declaredWeight(declaredWeight)
//                    .validatedWeight(request.getValidatedWeight())
//                    .adjustmentNote(request.getAdjustmentNote())
//                    .rejectionReason(null)
//                    .build();
//        } else {
//            validation.setDeclaredWeight(declaredWeight);
//            validation.setValidatedWeight(request.getValidatedWeight());
//            validation.setAdjustmentNote(request.getAdjustmentNote());
//            validation.setRejectionReason(null);
//        }
//
//        factoryValidationRepository.save(validation);
//
//        if (declaredWeight.compareTo(request.getValidatedWeight()) == 0) {
//            delivery.setStatus(FactoryDelivery.DeliveryStatus.VALIDATED);
//            delivery.setRecyclingCenter(userRecyclingCenter);
//        } else {
//            delivery.setStatus(FactoryDelivery.DeliveryStatus.ADJUSTED);
//            delivery.setRecyclingCenter(userRecyclingCenter);
//        }
//
//        if (delivery.getDeliveryDate() == null) {
//            delivery.setDeliveryDate(LocalDateTime.now());
//        }
//
//        factoryDeliveryRepository.save(delivery);
//
//        // ========== MISE À JOUR DU STOCK ==========
//        Material material = collectionRequest.getMaterial();
//        if (material != null) {
//            // Récupérer ou créer la ligne de stock pour ce matériau
//            MaterialStock stock = materialStockRepository.findByMaterialId(material.getId())
//                    .orElseGet(() -> {
//                        MaterialStock newStock = MaterialStock.builder()
//                                .material(material)
//                                .quantityKg(BigDecimal.ZERO)
//                                .build();
//                        return materialStockRepository.save(newStock);
//                    });
//            // Ajouter la quantité validée au stock
//            stock.setQuantityKg(stock.getQuantityKg().add(request.getValidatedWeight()));
//            materialStockRepository.save(stock);
//        } else {
//            // Log optionnel : matériau non défini, on ne met pas à jour le stock
//            // (peut arriver si la collecte n'a pas de matériau associé)
//        }
//        // =========================================
//
//        return toDetailResponse(delivery);
//    }

    @Transactional
    public WorkshopReceptionDetailResponse validateDelivery(Long deliveryId, ValidateDeliveryRequest request) {
        if (deliveryId == null) {
            throw new BadRequestException("L'identifiant de la livraison est obligatoire.");
        }

        if (request == null) {
            throw new BadRequestException("Les données de validation sont obligatoires.");
        }

        if (request.getValidatedWeight() == null) {
            throw new BadRequestException("Le poids validé est obligatoire.");
        }

        FactoryUser currentUser = getAuthenticatedFactoryUser();

        FactoryDelivery delivery = factoryDeliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Livraison introuvable."));

        Collection collection = delivery.getCollection();
        if (collection == null) {
            throw new ResourceNotFoundException("Aucune collecte liée à cette livraison.");
        }

        CollectionRequest collectionRequest = collection.getRequest();
        if (collectionRequest == null) {
            throw new ResourceNotFoundException("Aucune demande liée à cette collecte.");
        }

        // La demande doit rester COLLECTED côté client
        if (collectionRequest.getStatus() != CollectionRequest.Status.COLLECTED) {
            throw new BadRequestException("Seule une collecte au statut COLLECTED peut être validée par l'usine.");
        }

        BigDecimal declaredWeight = collection.getRealQuantity();
        if (declaredWeight == null) {
            throw new BadRequestException("Le poids déclaré de la collecte est introuvable.");
        }

        FactoryValidation validation = delivery.getValidation();
        RecyclingCenter userRecyclingCenter = currentUser.getRecyclingCenter();

        if (validation == null) {
            validation = FactoryValidation.builder()
                    .delivery(delivery)
                    .validatedBy(currentUser)
                    .declaredWeight(declaredWeight)
                    .validatedWeight(request.getValidatedWeight())
                    .adjustmentNote(request.getAdjustmentNote())
                    .rejectionReason(null)
                    .build();
        } else {
            validation.setDeclaredWeight(declaredWeight);
            validation.setValidatedWeight(request.getValidatedWeight());
            validation.setAdjustmentNote(request.getAdjustmentNote());
            validation.setRejectionReason(null);
        }

        // ========== CALCUL DU MONTANT COLLECTEUR (AJOUT) ==========
        BigDecimal collectorUnitPrice = collection.getCollectorUnitPrice();
        if (collectorUnitPrice == null) {
            // Fallback pour les anciennes collectes
            BigDecimal estimatedQuantity = collectionRequest.getEstimatedQuantity();
            BigDecimal collectorPriceTotal = collectionRequest.getCollectorPrice();
            if (estimatedQuantity != null && estimatedQuantity.compareTo(BigDecimal.ZERO) > 0 && collectorPriceTotal != null) {
                collectorUnitPrice = collectorPriceTotal.divide(estimatedQuantity, 3, RoundingMode.HALF_UP);
            } else {
                collectorUnitPrice = BigDecimal.ZERO;
            }
        }
        BigDecimal collectorAmount = request.getValidatedWeight().multiply(collectorUnitPrice)
                .setScale(3, RoundingMode.HALF_UP);
        validation.setCollectorAmount(collectorAmount);
        validation.setPaid(false);
        // ========================================================

        factoryValidationRepository.save(validation);

        if (declaredWeight.compareTo(request.getValidatedWeight()) == 0) {
            delivery.setStatus(FactoryDelivery.DeliveryStatus.VALIDATED);
            delivery.setRecyclingCenter(userRecyclingCenter);
        } else {
            delivery.setStatus(FactoryDelivery.DeliveryStatus.ADJUSTED);
            delivery.setRecyclingCenter(userRecyclingCenter);
        }

        if (delivery.getDeliveryDate() == null) {
            delivery.setDeliveryDate(LocalDateTime.now());
        }

        factoryDeliveryRepository.save(delivery);

        // ========== MISE À JOUR DU STOCK ==========
        Material material = collectionRequest.getMaterial();
        if (material != null) {
            MaterialStock stock = materialStockRepository.findByMaterialId(material.getId())
                    .orElseGet(() -> {
                        MaterialStock newStock = MaterialStock.builder()
                                .material(material)
                                .quantityKg(BigDecimal.ZERO)
                                .build();
                        return materialStockRepository.save(newStock);
                    });
            stock.setQuantityKg(stock.getQuantityKg().add(request.getValidatedWeight()));
            materialStockRepository.save(stock);
        }

        return toDetailResponse(delivery);
    }

    @Transactional
    public WorkshopReceptionDetailResponse rejectDelivery(Long deliveryId, RejectDeliveryRequest request) {
        if (deliveryId == null) {
            throw new BadRequestException("L'identifiant de la livraison est obligatoire.");
        }

        if (request == null || request.getRejectionReason() == null || request.getRejectionReason().isBlank()) {
            throw new BadRequestException("La raison du rejet est obligatoire.");
        }

        FactoryUser currentUser = getAuthenticatedFactoryUser();

        FactoryDelivery delivery = factoryDeliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Livraison introuvable."));

        Collection collection = delivery.getCollection();
        if (collection == null) {
            throw new ResourceNotFoundException("Aucune collecte liée à cette livraison.");
        }

        CollectionRequest collectionRequest = collection.getRequest();
        if (collectionRequest == null) {
            throw new ResourceNotFoundException("Aucune demande liée à cette collecte.");
        }

        if (collectionRequest.getStatus() != CollectionRequest.Status.COLLECTED) {
            throw new BadRequestException("Seule une collecte au statut COLLECTED peut être rejetée par l'usine.");
        }

        BigDecimal declaredWeight = collection.getRealQuantity();
        if (declaredWeight == null) {
            throw new BadRequestException("Le poids déclaré de la collecte est introuvable.");
        }

        FactoryValidation validation = delivery.getValidation();

        if (validation == null) {
            validation = FactoryValidation.builder()
                    .delivery(delivery)
                    .validatedBy(currentUser)
                    .declaredWeight(declaredWeight)
                    .validatedWeight(BigDecimal.ZERO)
                    .adjustmentNote(null)
                    .rejectionReason(request.getRejectionReason())
                    .build();
        } else {
            validation.setDeclaredWeight(declaredWeight);
            validation.setValidatedWeight(BigDecimal.ZERO);
            validation.setAdjustmentNote(null);
            validation.setRejectionReason(request.getRejectionReason());
        }

        factoryValidationRepository.save(validation);

        delivery.setStatus(FactoryDelivery.DeliveryStatus.REFUSED);
        factoryDeliveryRepository.save(delivery);

        return toDetailResponse(delivery);
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private FactoryUser getAuthenticatedFactoryUser() {
        String email = SecurityUtils.getEmail()
                .orElseThrow(() -> new UnauthorizedException("Utilisateur non authentifié."));

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable."));

        if (account.getRoleType() != Account.RoleType.FACTORY_USER) {
            throw new UnauthorizedException("Accès réservé au chef d’atelier.");
        }

        if (account.getFactoryUser() != null) {
            return account.getFactoryUser();
        }

        return factoryUserRepository.findAll().stream()
                .filter(fu -> fu.getAccount() != null && fu.getAccount().getId().equals(account.getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Profil employé usine introuvable."));
    }

    private WorkshopReceptionSummaryResponse toSummaryResponse(FactoryDelivery delivery) {
        Collection collection = delivery.getCollection();
        CollectionRequest request = collection != null ? collection.getRequest() : null;
        Client client = request != null ? request.getClient() : null;
        Collector collector = collection != null ? collection.getCollector() : null;

        return WorkshopReceptionSummaryResponse.builder()
                .deliveryId(delivery.getId())
                .status(delivery.getStatus() != null ? delivery.getStatus().name() : null)
                .deliveryDate(delivery.getDeliveryDate())
                .createdAt(delivery.getCreatedAt())
                .collectionId(collection != null ? collection.getId() : null)
                .requestId(request != null ? request.getId() : null)
                .clientId(client != null ? client.getId() : null)
                .collectorId(collector != null ? collector.getId() : null)
                .declaredWeight(collection != null ? collection.getRealQuantity() : null)
                .build();
    }

    private WorkshopReceptionDetailResponse toDetailResponse(FactoryDelivery delivery) {
        Collection collection = delivery.getCollection();
        CollectionRequest request = collection != null ? collection.getRequest() : null;
        Client client = request != null ? request.getClient() : null;
        Collector collector = collection != null ? collection.getCollector() : null;
        FactoryValidation validation = delivery.getValidation();

        return WorkshopReceptionDetailResponse.builder()
                .deliveryId(delivery.getId())
                .status(delivery.getStatus() != null ? delivery.getStatus().name() : null)
                .deliveryDate(delivery.getDeliveryDate())
                .createdAt(delivery.getCreatedAt())
                .collectionId(collection != null ? collection.getId() : null)
                .requestId(request != null ? request.getId() : null)
                .clientId(client != null ? client.getId() : null)
                .collectorId(collector != null ? collector.getId() : null)
                .declaredWeight(collection != null ? collection.getRealQuantity() : null)
                .validatedWeight(validation != null ? validation.getValidatedWeight() : null)
                .adjustmentNote(validation != null ? validation.getAdjustmentNote() : null)
                .rejectionReason(validation != null ? validation.getRejectionReason() : null)
                .validatedById(validation != null && validation.getValidatedBy() != null
                        ? validation.getValidatedBy().getId()
                        : null)
                .build();
    }

    // Dans WorkshopReceptionService.java

    @Transactional(readOnly = true)
    public CollectionFullDetailsDTO getCollectionFullDetails(Long collectionId) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Collecte introuvable"));

        CollectionRequest request = collection.getRequest();
        if (request == null) {
            throw new ResourceNotFoundException("Aucune demande associée à cette collecte");
        }

        Client client = request.getClient();
        Account clientAccount = client != null ? client.getAccount() : null;

        Collector collector = collection.getCollector();
        Account collectorAccount = collector != null ? collector.getAccount() : null;

        FactoryDelivery delivery = collection.getFactoryDelivery();
        FactoryValidation validation = delivery != null ? delivery.getValidation() : null;
        FactoryUser validator = validation != null ? validation.getValidatedBy() : null;

        return CollectionFullDetailsDTO.builder()
                // Collection
                .collectionId(collection.getId())
                .realQuantity(collection.getRealQuantity())
                .totalAmount(collection.getTotalAmount())
                .collectionProofImageUrl(collection.getCollectionProofImageUrl())
                .collectedAt(collection.getCollectedAt())
                .paymentStatus(collection.getPaymentStatus() != null ? collection.getPaymentStatus().name() : null)

                // Request
                .requestId(request.getId())
                .requestStatus(request.getStatus() != null ? request.getStatus().name() : null)
                .estimatedQuantity(request.getEstimatedQuantity())
                .estimatedAmount(request.getEstimatedAmount())
                .collectorPrice(request.getCollectorPrice())
                .wasteImageUrl(request.getWasteImageUrl())
                .address(client != null ? client.getAddress() : null)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())

                // Client
                .clientId(client != null ? client.getId() : null)
                .clientFirstName(client != null ? client.getFirstName() : null)
                .clientLastName(client != null ? client.getLastName() : null)
                .clientEmail(clientAccount != null ? clientAccount.getEmail() : null)
                .clientPhone(clientAccount != null ? clientAccount.getPhone() : null)

                // Collecteur
                .collectorId(collector != null ? collector.getId() : null)
                .collectorFirstName(collector != null ? collector.getFirstName() : null)
                .collectorLastName(collector != null ? collector.getLastName() : null)
                .collectorEmail(collectorAccount != null ? collectorAccount.getEmail() : null)
                .collectorPhone(collectorAccount != null ? collectorAccount.getPhone() : null)
                .isCollectorVerified(collector != null ? collector.getIsVerified() : null)
                .collectorAverageRating(collector != null ? collector.getAverageRating() : null)

                // Livraison
                .deliveryId(delivery != null ? delivery.getId() : null)
                .deliveryStatus(delivery != null && delivery.getStatus() != null ? delivery.getStatus().name() : null)
                .deliveryDate(delivery != null ? delivery.getDeliveryDate() : null)
                .deliveryCreatedAt(delivery != null ? delivery.getCreatedAt() : null)

                // Validation
                .validationId(validation != null ? validation.getId() : null)
                .declaredWeight(validation != null ? validation.getDeclaredWeight() : null)
                .validatedWeight(validation != null ? validation.getValidatedWeight() : null)
                .adjustmentNote(validation != null ? validation.getAdjustmentNote() : null)
                .rejectionReason(validation != null ? validation.getRejectionReason() : null)
                .validatedByUserId(validator != null ? validator.getId() : null)
                .validatedAt(validation != null ? validation.getValidatedAt() : null)
                .build();
    }

    // =========================================================
    // REQUEST / RESPONSE CLASSES
    // =========================================================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReceiveCollectionRequest {
        private LocalDateTime deliveryDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ValidateDeliveryRequest {
        @NotNull(message = "Le poids validé est obligatoire.")
        private BigDecimal validatedWeight;

        private String adjustmentNote;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RejectDeliveryRequest {
        @NotBlank(message = "La raison du rejet est obligatoire.")
        private String rejectionReason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WorkshopReceptionSummaryResponse {
        private Long deliveryId;
        private String status;
        private LocalDateTime deliveryDate;
        private LocalDateTime createdAt;
        private Long collectionId;
        private Long requestId;
        private Long clientId;
        private Long collectorId;
        private BigDecimal declaredWeight;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WorkshopReceptionDetailResponse {
        private Long deliveryId;
        private String status;
        private LocalDateTime deliveryDate;
        private LocalDateTime createdAt;
        private Long collectionId;
        private Long requestId;
        private Long clientId;
        private Long collectorId;
        private BigDecimal declaredWeight;
        private BigDecimal validatedWeight;
        private String adjustmentNote;
        private String rejectionReason;
        private Long validatedById;
    }
}