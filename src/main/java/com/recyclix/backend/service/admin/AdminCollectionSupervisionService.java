// recyclix\backend\service\admin\AdminCollectionSupervisionService.java
package com.recyclix.backend.service.admin;

import com.recyclix.backend.dto.admin.*;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.model.*;
import com.recyclix.backend.model.Collection;
import com.recyclix.backend.model.CollectionRequest.Status;
import com.recyclix.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCollectionSupervisionService {

    private final CollectionRequestRepository requestRepo;
    private final CollectionRepository collectionRepo;
    private final FactoryDeliveryRepository deliveryRepo;
    private final FactoryValidationRepository validationRepo;
    private final SupportTicketRepository ticketRepo;
    private final AccountRepository accountRepo;

    // ============================================================
    // 1. STATISTIQUES DE SUPERVISION
    // ============================================================
    public SupervisionStatsDTO getSupervisionStats() {

        List<CollectionRequest> allRequests = requestRepo.findAll();
        List<FactoryDelivery> allDeliveries = deliveryRepo.findAll();

        long totalRequests = allRequests.size();
        long pendingRequests = countByStatus(allRequests, Status.PENDING);
        long acceptedRequests = countByStatus(allRequests, Status.ACCEPTED);
        long collectedRequests = countByStatus(allRequests, Status.COLLECTED);
        long deliveredRequests = countByStatus(allRequests, Status.DELIVERED);
        long cancelledRequests = countByStatus(allRequests, Status.CANCELLED);

        long disputedRequests = countDisputedRequests(allRequests);

        long collectionsPendingValidation = allDeliveries.stream()
                .filter(d -> d.getStatus() == FactoryDelivery.DeliveryStatus.PENDING)
                .count();

        long totalCollections = collectionRepo.count();
        long totalDeliveries = allDeliveries.size();

        long validatedDeliveries = allDeliveries.stream()
                .filter(d -> d.getStatus() == FactoryDelivery.DeliveryStatus.VALIDATED
                        || d.getStatus() == FactoryDelivery.DeliveryStatus.COMPLETED)
                .count();

        long refusedDeliveries = allDeliveries.stream()
                .filter(d -> d.getStatus() == FactoryDelivery.DeliveryStatus.REFUSED)
                .count();

        return SupervisionStatsDTO.builder()
                .totalRequests(totalRequests)
                .pendingRequests(pendingRequests)
                .acceptedRequests(acceptedRequests)
                .collectedRequests(collectedRequests)
                .deliveredRequests(deliveredRequests)
                .cancelledRequests(cancelledRequests)
                .disputedRequests(disputedRequests)
                .collectionsPendingValidation(collectionsPendingValidation)
                .totalCollections(totalCollections)
                .totalDeliveries(totalDeliveries)
                .validatedDeliveries(validatedDeliveries)
                .refusedDeliveries(refusedDeliveries)
                .build();
    }

    // ============================================================
    // 2. LISTE DES DEMANDES (FILTRES + PAGINATION)
    // ============================================================
    public Page<CollectionRequestSupervisionDTO> getAllRequests(
            Status status,
            Long clientId,
            Long collectorId,
            Long materialId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int size
    ) {
        List<CollectionRequest> requests = requestRepo.findAll();

        List<CollectionRequest> filtered = requests.stream()
                .filter(r -> status == null || r.getStatus() == status)
                .filter(r -> clientId == null || (r.getClient() != null && r.getClient().getId().equals(clientId)))
                .filter(r -> collectorId == null || (r.getCollection() != null
                        && r.getCollection().getCollector() != null
                        && r.getCollection().getCollector().getId().equals(collectorId)))
                .filter(r -> materialId == null || (r.getMaterial() != null && r.getMaterial().getId().equals(materialId)))
                .filter(r -> startDate == null || !r.getCreatedAt().isBefore(startDate))
                .filter(r -> endDate == null || !r.getCreatedAt().isAfter(endDate))
                .sorted(Comparator.comparing(CollectionRequest::getCreatedAt).reversed())
                .toList();

        int start = page * size;
        int end = Math.min(start + size, filtered.size());

        if (start >= filtered.size()) {
            return new PageImpl<>(List.of(), PageRequest.of(page, size), filtered.size());
        }

        List<CollectionRequestSupervisionDTO> dtos = filtered.subList(start, end).stream()
                .map(this::toSupervisionDTO)
                .toList();

        return new PageImpl<>(dtos, PageRequest.of(page, size), filtered.size());
    }

    // ============================================================
    // 3. DÉTAIL COMPLET D'UNE DEMANDE
    // ============================================================
    public RequestFullDetailDTO getRequestDetail(Long requestId) {
        if (requestId == null) {
            throw new BadRequestException("L'identifiant de la demande est obligatoire.");
        }

        CollectionRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande de collecte introuvable avec l'ID : " + requestId));

        Client client = request.getClient();
        Account clientAccount = (client != null) ? client.getAccount() : null;

        Material material = request.getMaterial();
        AIClassification ai = request.getAiClassification();

        Collection collection = request.getCollection();
        Collector collector = (collection != null) ? collection.getCollector() : null;
        Account collectorAccount = (collector != null) ? collector.getAccount() : null;

        FactoryDelivery delivery = (collection != null) ? collection.getFactoryDelivery() : null;
        FactoryValidation validation = (delivery != null) ? delivery.getValidation() : null;
        FactoryUser validator = (validation != null) ? validation.getValidatedBy() : null;

        boolean hasOpenTicket = ticketRepo.findAllByAccountId(clientAccount != null ? clientAccount.getId() : null)
                .stream()
                .anyMatch(t -> t.getStatus() == SupportTicket.Status.OPEN
                        || t.getStatus() == SupportTicket.Status.IN_PROGRESS);

        SupportTicket relatedTicket = ticketRepo.findAllByAccountId(clientAccount != null ? clientAccount.getId() : null)
                .stream()
                .filter(t -> t.getSubject() != null && t.getSubject().contains("Demande #" + requestId))
                .findFirst()
                .orElse(null);

        return RequestFullDetailDTO.builder()
                .requestId(request.getId())
                .requestStatus(request.getStatus())
                .estimatedQuantity(request.getEstimatedQuantity())
                .estimatedAmount(request.getEstimatedAmount())
                .collectorPrice(request.getCollectorPrice())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .wasteImageUrl(request.getWasteImageUrl())
                .requestCreatedAt(request.getCreatedAt())
                .requestUpdatedAt(request.getUpdatedAt())

                .clientId(client != null ? client.getId() : null)
                .clientFullName(client != null ? client.getFirstName() + " " + client.getLastName() : null)
                .clientEmail(clientAccount != null ? clientAccount.getEmail() : null)
                .clientPhone(clientAccount != null ? clientAccount.getPhone() : null)
                .clientAddress(client != null ? client.getAddress() : null)

                .materialId(material != null ? material.getId() : null)
                .materialName(material != null ? material.getName() : null)

                .aiClassificationId(ai != null ? ai.getId() : null)
                .predictedMaterialId(ai != null ? ai.getPredictedMaterialId() : null)
                .predictedWeight(ai != null ? ai.getPredictedWeight() : null)
                .confidenceScore(ai != null ? ai.getConfidenceScore() : null)
                .aiValidated(ai != null ? ai.getIsValidated() : null)

                .collectorId(collector != null ? collector.getId() : null)
                .collectorFullName(collector != null ? collector.getFirstName() + " " + collector.getLastName() : null)
                .collectorEmail(collectorAccount != null ? collectorAccount.getEmail() : null)
                .collectorPhone(collectorAccount != null ? collectorAccount.getPhone() : null)

                .collectionId(collection != null ? collection.getId() : null)
                .realQuantity(collection != null ? collection.getRealQuantity() : null)
                .unitPriceFrozen(collection != null ? collection.getUnitPriceFrozen() : null)
                .totalAmount(collection != null ? collection.getTotalAmount() : null)
                .paymentStatus(collection != null ? collection.getPaymentStatus() : null)
                .collectionProofImageUrl(collection != null ? collection.getCollectionProofImageUrl() : null)
                .collectedAt(collection != null ? collection.getCollectedAt() : null)

                .deliveryId(delivery != null ? delivery.getId() : null)
                .deliveryStatus(delivery != null && delivery.getStatus() != null ? delivery.getStatus().name() : null)
                .deliveryDate(delivery != null ? delivery.getDeliveryDate() : null)

                .validationId(validation != null ? validation.getId() : null)
                .declaredWeight(validation != null ? validation.getDeclaredWeight() : null)
                .validatedWeight(validation != null ? validation.getValidatedWeight() : null)
                .adjustmentNote(validation != null ? validation.getAdjustmentNote() : null)
                .rejectionReason(validation != null ? validation.getRejectionReason() : null)
                .validatedById(validator != null ? validator.getId() : null)
                .validatedByFullName(validator != null ? validator.getFirstName() + " " + validator.getLastName() : null)
                .validatedAt(validation != null ? validation.getValidatedAt() : null)

                .hasOpenTicket(hasOpenTicket)
                .relatedTicketId(relatedTicket != null ? relatedTicket.getId() : null)
                .ticketSubject(relatedTicket != null ? relatedTicket.getSubject() : null)
                .build();
    }

    // ============================================================
    // 4. DÉTECTION DE LITIGES
    // ============================================================
    public Page<DisputedRequestDTO> getDisputedRequests(int page, int size) {
        List<CollectionRequest> allRequests = requestRepo.findAll();

        List<CollectionRequest> disputed = allRequests.stream()
                .filter(r -> {
                    boolean cancelled = r.getStatus() == Status.CANCELLED;
                    boolean hasOpenTicket = hasOpenTicketForRequest(r);
                    return cancelled || hasOpenTicket;
                })
                .sorted(Comparator.comparing(CollectionRequest::getCreatedAt).reversed())
                .toList();

        int start = page * size;
        int end = Math.min(start + size, disputed.size());

        if (start >= disputed.size()) {
            return new PageImpl<>(List.of(), PageRequest.of(page, size), disputed.size());
        }

        List<DisputedRequestDTO> dtos = disputed.subList(start, end).stream()
                .map(this::toDisputedDTO)
                .toList();

        return new PageImpl<>(dtos, PageRequest.of(page, size), disputed.size());
    }

    // ============================================================
    // 5. ANNULATION FORCÉE PAR L'ADMIN
    // ============================================================
    @Transactional
    public RequestFullDetailDTO forceCancelRequest(Long requestId, String reason) {
        if (requestId == null) {
            throw new BadRequestException("L'identifiant de la demande est obligatoire.");
        }

        if (reason == null || reason.isBlank()) {
            reason = "Annulation forcée par l'administrateur";
        }

        CollectionRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande de collecte introuvable avec l'ID : " + requestId));

        if (request.getStatus() == Status.CANCELLED) {
            throw new BadRequestException("Cette demande est déjà annulée.");
        }

        if (request.getStatus() == Status.COLLECTED || request.getStatus() == Status.DELIVERED) {
            throw new BadRequestException("Impossible d'annuler une demande déjà collectée ou livrée.");
        }

        request.setStatus(Status.ADMIN_CANCELLED);
        requestRepo.save(request);

        return getRequestDetail(requestId);
    }

    // ============================================================
    // 6. RÉASSIGNATION D'UNE DEMANDE (optionnel avancé)
    // ============================================================
    @Transactional
    public RequestFullDetailDTO reassignRequest(Long requestId, Long collectorId) {
        if (requestId == null) {
            throw new BadRequestException("L'identifiant de la demande est obligatoire.");
        }

        if (collectorId == null) {
            throw new BadRequestException("L'identifiant du collecteur est obligatoire.");
        }

        CollectionRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande de collecte introuvable avec l'ID : " + requestId));

        if (request.getStatus() != Status.PENDING && request.getStatus() != Status.ACCEPTED) {
            throw new BadRequestException("Seule une demande en attente ou acceptée peut être réassignée.");
        }

        if (request.getCollection() != null) {
            throw new BadRequestException("Cette demande a déjà une collecte associée.");
        }

        Collection collection = request.getCollection();
        if (collection != null) {
            Collection existingCollection = collectionRepo.findById(collection.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Collecte introuvable."));

            existingCollection.setCollector(null);
            collectionRepo.save(existingCollection);
        }

        request.setStatus(Status.PENDING);
        requestRepo.save(request);

        return getRequestDetail(requestId);
    }

    // ============================================================
    // HELPERS PRIVÉS
    // ============================================================

    private long countByStatus(List<CollectionRequest> requests, Status status) {
        return requests.stream().filter(r -> r.getStatus() == status).count();
    }

    private long countDisputedRequests(List<CollectionRequest> requests) {
        return requests.stream().filter(r -> {
            boolean cancelled = r.getStatus() == Status.CANCELLED;
            boolean hasOpenTicket = hasOpenTicketForRequest(r);
            return cancelled || hasOpenTicket;
        }).count();
    }

    private boolean hasOpenTicketForRequest(CollectionRequest request) {
        if (request.getClient() == null || request.getClient().getAccount() == null) {
            return false;
        }

        Long accountId = request.getClient().getAccount().getId();

        return ticketRepo.findAllByAccountId(accountId).stream()
                .anyMatch(t -> t.getStatus() == SupportTicket.Status.OPEN
                        || t.getStatus() == SupportTicket.Status.IN_PROGRESS);
    }

    private CollectionRequestSupervisionDTO toSupervisionDTO(CollectionRequest r) {
        Client client = r.getClient();
        Account clientAccount = (client != null) ? client.getAccount() : null;

        Collection collection = r.getCollection();
        Collector collector = (collection != null) ? collection.getCollector() : null;

        FactoryDelivery delivery = (collection != null) ? collection.getFactoryDelivery() : null;
        FactoryValidation validation = (delivery != null) ? delivery.getValidation() : null;

        boolean hasOpenTicket = hasOpenTicketForRequest(r);

        return CollectionRequestSupervisionDTO.builder()
                .requestId(r.getId())
                .status(r.getStatus())

                .clientId(client != null ? client.getId() : null)
                .clientFullName(client != null ? client.getFirstName() + " " + client.getLastName() : null)
                .clientEmail(clientAccount != null ? clientAccount.getEmail() : null)

                .materialId(r.getMaterial() != null ? r.getMaterial().getId() : null)
                .materialName(r.getMaterial() != null ? r.getMaterial().getName() : null)

                .estimatedQuantity(r.getEstimatedQuantity())
                .estimatedAmount(r.getEstimatedAmount())
                .collectorPrice(r.getCollectorPrice())

                .collectorId(collector != null ? collector.getId() : null)
                .collectorFullName(collector != null ? collector.getFirstName() + " " + collector.getLastName() : null)

                .collectionId(collection != null ? collection.getId() : null)
                .realQuantity(collection != null ? collection.getRealQuantity() : null)
                .totalAmount(collection != null ? collection.getTotalAmount() : null)

                .deliveryStatus(delivery != null && delivery.getStatus() != null ? delivery.getStatus().name() : null)
                .validationStatus(validation != null
                        ? (validation.getRejectionReason() != null ? "REFUSED" : "VALIDATED")
                        : null)

                .hasOpenTicket(hasOpenTicket)

                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }

    private DisputedRequestDTO toDisputedDTO(CollectionRequest r) {
        Client client = r.getClient();
        Account clientAccount = (client != null) ? client.getAccount() : null;

        Collection collection = r.getCollection();
        Collector collector = (collection != null) ? collection.getCollector() : null;

        String disputeReason;

        // Déterminer le motif du litige selon le statut
        if (r.getStatus() == Status.ADMIN_CANCELLED) {
            disputeReason = "Annulée par l'administrateur";
        } else if (r.getStatus() == Status.CANCELLED) {
            disputeReason = "Annulée par le citoyen";
        } else {
            // Autres cas : ticket support ouvert ou rejet usine
            disputeReason = "Ticket support ouvert";
            if (collection != null && collection.getFactoryDelivery() != null
                    && collection.getFactoryDelivery().getStatus() == FactoryDelivery.DeliveryStatus.REFUSED) {
                disputeReason = "Rejetée par l'usine";
            }
        }

        SupportTicket relatedTicket = ticketRepo.findAllByAccountId(clientAccount != null ? clientAccount.getId() : null)
                .stream()
                .filter(t -> t.getSubject() != null && t.getSubject().contains("Demande #" + r.getId()))
                .findFirst()
                .orElse(null);

        return DisputedRequestDTO.builder()
                .requestId(r.getId())
                .requestStatus(r.getStatus())
                .clientId(client != null ? client.getId() : null)
                .clientFullName(client != null ? client.getFirstName() + " " + client.getLastName() : null)
                .collectorId(collector != null ? collector.getId() : null)
                .collectorFullName(collector != null ? collector.getFirstName() + " " + collector.getLastName() : null)
                .estimatedAmount(r.getEstimatedAmount())
                .disputeReason(disputeReason)
                .relatedTicketId(relatedTicket != null ? relatedTicket.getId() : null)
                .ticketSubject(relatedTicket != null ? relatedTicket.getSubject() : null)
                .ticketStatus(relatedTicket != null ? relatedTicket.getStatus().name() : null)
                .createdAt(r.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public Page<DeliveryValidationDTO> getDeliveriesWithValidation(
            FactoryDelivery.DeliveryStatus status,
            Long clientId, Long collectorId, Long materialId,
            int page, int size) {

        List<FactoryDelivery> deliveries = deliveryRepo.findAll();

        List<DeliveryValidationDTO> result = deliveries.stream()
                .filter(d -> status == null || d.getStatus() == status)
                .filter(d -> clientId == null || (d.getCollection() != null && d.getCollection().getRequest() != null &&
                        d.getCollection().getRequest().getClient() != null &&
                        d.getCollection().getRequest().getClient().getId().equals(clientId)))
                .filter(d -> collectorId == null || (d.getCollection() != null && d.getCollection().getCollector() != null &&
                        d.getCollection().getCollector().getId().equals(collectorId)))
                .filter(d -> materialId == null || (d.getCollection() != null && d.getCollection().getRequest() != null &&
                        d.getCollection().getRequest().getMaterial() != null &&
                        d.getCollection().getRequest().getMaterial().getId().equals(materialId)))
                .map(this::toDeliveryValidationDTO)
                .sorted(Comparator.comparing(DeliveryValidationDTO::getDeliveryId).reversed())
                .toList();

        int start = page * size;
        int end = Math.min(start + size, result.size());
        if (start >= result.size()) {
            return new PageImpl<>(List.of(), PageRequest.of(page, size), result.size());
        }
        return new PageImpl<>(result.subList(start, end), PageRequest.of(page, size), result.size());
    }

    // Dans AdminCollectionSupervisionService.java, méthode toDeliveryValidationDTO
    private DeliveryValidationDTO toDeliveryValidationDTO(FactoryDelivery delivery) {
        Collection c = delivery.getCollection();
        CollectionRequest r = c != null ? c.getRequest() : null;
        Client client = r != null ? r.getClient() : null;
        Collector collector = c != null ? c.getCollector() : null;
        Material material = r != null ? r.getMaterial() : null;
        FactoryValidation v = delivery.getValidation();

        String validatorName = null;
        if (v != null && v.getValidatedBy() != null) {
            validatorName = v.getValidatedBy().getFirstName() + " " + v.getValidatedBy().getLastName();
        }

        // ✅ Récupération du nom de l'usine
        String centerName = null;
        if (delivery.getRecyclingCenter() != null) {
            centerName = delivery.getRecyclingCenter().getName();
        }

        return DeliveryValidationDTO.builder()
                .deliveryId(delivery.getId())
                .collectionId(c != null ? c.getId() : null)
                .clientId(client != null ? client.getId() : null)
                .clientName(client != null ? client.getFirstName() + " " + client.getLastName() : null)
                .collectorId(collector != null ? collector.getId() : null)
                .collectorName(collector != null ? collector.getFirstName() + " " + collector.getLastName() : null)
                .materialName(material != null ? material.getName() : null)
                .declaredWeight(c != null ? c.getRealQuantity() : null)
                .validatedWeight(v != null ? v.getValidatedWeight() : null)
                .status(delivery.getStatus() != null ? delivery.getStatus().name() : null)
                .adjustmentNote(v != null ? v.getAdjustmentNote() : null)
                .rejectionReason(v != null ? v.getRejectionReason() : null)
                .validatedByName(validatorName)
                .validatedAt(v != null ? v.getValidatedAt() : null)
                .wastePhoto(c != null ? c.getRequest().getWasteImageUrl() : null)
                .recyclingCenterName(centerName)   // ✅ NOUVEAU
                .build();
    }

}