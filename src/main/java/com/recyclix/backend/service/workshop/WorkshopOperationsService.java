package com.recyclix.backend.service.workshop;

import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.model.Collection;
import com.recyclix.backend.model.CollectionRequest;
import com.recyclix.backend.model.FactoryDelivery;
import com.recyclix.backend.model.Material;
import com.recyclix.backend.repository.CollectionRepository;
import com.recyclix.backend.repository.FactoryDeliveryRepository;
import com.recyclix.backend.repository.MaterialRepository;
import lombok.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkshopOperationsService {

    private final FactoryDeliveryRepository factoryDeliveryRepository;
    private final MaterialRepository materialRepository;
    private final CollectionRepository collectionRepository;

    // =========================================================
    // PROCESSING
    // =========================================================

    @Transactional(readOnly = true)
    public List<ProcessingItemResponse> getProcessingItems() {
        return factoryDeliveryRepository.findAll().stream()
                .filter(this::isProcessingDelivery)
                .map(this::toProcessingItemResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProcessingItemResponse> getCompletedItems() {
        return factoryDeliveryRepository.findAll().stream()
                .filter(this::isCompletedDelivery)
                .map(this::toProcessingItemResponse)
                .toList();
    }

    public ProcessingItemResponse startProcessing(Long deliveryId) {
        if (deliveryId == null) {
            throw new BadRequestException("L'identifiant de la livraison est obligatoire.");
        }

        FactoryDelivery delivery = factoryDeliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Livraison introuvable."));

        Collection collection = requireCollection(delivery);

        if (collection.getRequest() == null) {
            throw new ResourceNotFoundException("Aucune demande liée à cette collecte.");
        }

        // La demande client reste arrêtée à COLLECTED
        if (collection.getRequest().getStatus() != CollectionRequest.Status.COLLECTED) {
            throw new BadRequestException("Seule une collecte au statut COLLECTED peut être traitée par l'usine.");
        }

        if (delivery.getStatus() != FactoryDelivery.DeliveryStatus.VALIDATED
                && delivery.getStatus() != FactoryDelivery.DeliveryStatus.ADJUSTED) {
            throw new BadRequestException("Seule une livraison validée ou ajustée peut passer en traitement.");
        }

        delivery.setStatus(FactoryDelivery.DeliveryStatus.PROCESSING);
        factoryDeliveryRepository.save(delivery);

        return toProcessingItemResponse(delivery);
    }

    public ProcessingItemResponse completeProcessing(Long deliveryId) {
        if (deliveryId == null) {
            throw new BadRequestException("L'identifiant de la livraison est obligatoire.");
        }

        FactoryDelivery delivery = factoryDeliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Livraison introuvable."));

        Collection collection = requireCollection(delivery);

        if (collection.getRequest() == null) {
            throw new ResourceNotFoundException("Aucune demande liée à cette collecte.");
        }

        if (delivery.getStatus() != FactoryDelivery.DeliveryStatus.PROCESSING) {
            throw new BadRequestException("Seule une livraison en cours de traitement peut être terminée.");
        }

        delivery.setStatus(FactoryDelivery.DeliveryStatus.COMPLETED);
        factoryDeliveryRepository.save(delivery);

        return toProcessingItemResponse(delivery);
    }

    // =========================================================
    // STOCK
    // =========================================================

    @Transactional(readOnly = true)
    public StockOverviewResponse getStockOverview() {
        List<FactoryDelivery> deliveries = factoryDeliveryRepository.findAll();

        List<FactoryDelivery> countedDeliveries = deliveries.stream()
                .filter(d -> d.getCollection() != null)
                .filter(d -> isCountedInStock(d.getStatus()))
                .toList();

        BigDecimal totalWeight = countedDeliveries.stream()
                .map(FactoryDelivery::getCollection)
                .filter(Objects::nonNull)
                .map(Collection::getRealQuantity)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalItems = countedDeliveries.size();

        long completedItems = countedDeliveries.stream()
                .filter(d -> d.getStatus() == FactoryDelivery.DeliveryStatus.COMPLETED)
                .count();

        long processingItems = countedDeliveries.stream()
                .filter(d -> d.getStatus() == FactoryDelivery.DeliveryStatus.PROCESSING)
                .count();

        return StockOverviewResponse.builder()
                .totalCollections(totalItems)
                .totalWeight(totalWeight)
                .processingItems(processingItems)
                .completedItems(completedItems)
                .build();
    }

    @Transactional(readOnly = true)
    public List<MaterialStockResponse> getStockByMaterial() {
        List<Material> materials = materialRepository.findAll();
        List<FactoryDelivery> deliveries = factoryDeliveryRepository.findAll();

        Map<Long, List<FactoryDelivery>> deliveriesByMaterial = deliveries.stream()
                .filter(d -> d.getCollection() != null)
                .filter(d -> d.getCollection().getRequest() != null)
                .filter(d -> d.getCollection().getRequest().getMaterial() != null)
                .filter(d -> isCountedInStock(d.getStatus()))
                .collect(Collectors.groupingBy(d -> d.getCollection().getRequest().getMaterial().getId()));

        return materials.stream()
                .map(material -> {
                    List<FactoryDelivery> materialDeliveries = deliveriesByMaterial.getOrDefault(material.getId(), List.of());

                    BigDecimal totalWeight = materialDeliveries.stream()
                            .map(FactoryDelivery::getCollection)
                            .filter(Objects::nonNull)
                            .map(Collection::getRealQuantity)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    long totalCollections = materialDeliveries.size();

                    return MaterialStockResponse.builder()
                            .materialId(material.getId())
                            .materialName(material.getName())
                            .isActive(material.getIsActive())
                            .totalCollections(totalCollections)
                            .totalWeight(totalWeight)
                            .build();
                })
                .toList();
    }

    // =========================================================
    // PRODUCTION
    // =========================================================

    @Transactional(readOnly = true)
    public ProductionOverviewResponse getProductionOverview() {
        List<FactoryDelivery> deliveries = factoryDeliveryRepository.findAll();

        List<FactoryDelivery> completedDeliveries = deliveries.stream()
                .filter(d -> d.getCollection() != null)
                .filter(d -> d.getStatus() == FactoryDelivery.DeliveryStatus.COMPLETED)
                .toList();

        List<FactoryDelivery> processingDeliveries = deliveries.stream()
                .filter(d -> d.getCollection() != null)
                .filter(d -> d.getStatus() == FactoryDelivery.DeliveryStatus.PROCESSING)
                .toList();

        BigDecimal completedWeight = completedDeliveries.stream()
                .map(FactoryDelivery::getCollection)
                .filter(Objects::nonNull)
                .map(Collection::getRealQuantity)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal processingWeight = processingDeliveries.stream()
                .map(FactoryDelivery::getCollection)
                .filter(Objects::nonNull)
                .map(Collection::getRealQuantity)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long completedCount = completedDeliveries.size();
        long processingCount = processingDeliveries.size();
        long totalHandled = completedCount + processingCount;

        BigDecimal completionRate = BigDecimal.ZERO;
        if (totalHandled > 0) {
            completionRate = BigDecimal.valueOf(completedCount * 100.0 / totalHandled);
        }

        return ProductionOverviewResponse.builder()
                .processingCount(processingCount)
                .completedCount(completedCount)
                .processingWeight(processingWeight)
                .completedWeight(completedWeight)
                .completionRatePercent(completionRate)
                .build();
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private Collection requireCollection(FactoryDelivery delivery) {
        if (delivery.getCollection() == null) {
            throw new ResourceNotFoundException("Aucune collecte liée à cette livraison.");
        }
        return delivery.getCollection();
    }

    private boolean isProcessingDelivery(FactoryDelivery delivery) {
        return delivery.getStatus() == FactoryDelivery.DeliveryStatus.PROCESSING;
    }

    private boolean isCompletedDelivery(FactoryDelivery delivery) {
        return delivery.getStatus() == FactoryDelivery.DeliveryStatus.COMPLETED;
    }

    private boolean isCountedInStock(FactoryDelivery.DeliveryStatus status) {
        return status == FactoryDelivery.DeliveryStatus.VALIDATED
                || status == FactoryDelivery.DeliveryStatus.ADJUSTED
                || status == FactoryDelivery.DeliveryStatus.PROCESSING
                || status == FactoryDelivery.DeliveryStatus.COMPLETED;
    }

    private ProcessingItemResponse toProcessingItemResponse(FactoryDelivery delivery) {
        Collection collection = delivery.getCollection();
        CollectionRequest request = collection != null ? collection.getRequest() : null;
        Material material = request != null ? request.getMaterial() : null;

        return ProcessingItemResponse.builder()
                .deliveryId(delivery.getId())
                .collectionId(collection != null ? collection.getId() : null)
                .requestId(request != null ? request.getId() : null)
                .materialId(material != null ? material.getId() : null)
                .materialName(material != null ? material.getName() : null)
                .status(delivery.getStatus() != null ? delivery.getStatus().name() : null)
                .weight(collection != null ? collection.getRealQuantity() : null)
                .deliveryDate(delivery.getDeliveryDate())
                .createdAt(delivery.getCreatedAt())
                .build();
    }

    // =========================================================
    // RESPONSE CLASSES
    // =========================================================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProcessingItemResponse {
        private Long deliveryId;
        private Long collectionId;
        private Long requestId;
        private Long materialId;
        private String materialName;
        private String status;
        private BigDecimal weight;
        private LocalDateTime deliveryDate;
        private LocalDateTime createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StockOverviewResponse {
        private Long totalCollections;
        private BigDecimal totalWeight;
        private Long processingItems;
        private Long completedItems;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MaterialStockResponse {
        private Long materialId;
        private String materialName;
        private Boolean isActive;
        private Long totalCollections;
        private BigDecimal totalWeight;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductionOverviewResponse {
        private Long processingCount;
        private Long completedCount;
        private BigDecimal processingWeight;
        private BigDecimal completedWeight;
        private BigDecimal completionRatePercent;
    }
}