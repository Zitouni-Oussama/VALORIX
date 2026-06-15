package com.recyclix.backend.service.admin;

import com.recyclix.backend.dto.material.MaterialResponseDTO;
import com.recyclix.backend.dto.material.MaterialSummaryDTO;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ConflictException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.mapper.MaterialMapper;
import com.recyclix.backend.model.Material;
import com.recyclix.backend.model.MaterialPrice;
import com.recyclix.backend.repository.MaterialPriceRepository;
import com.recyclix.backend.repository.MaterialRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminPricingService {

    private final MaterialRepository materialRepository;
    private final MaterialPriceRepository materialPriceRepository;
    private final MaterialMapper materialMapper;

    @Transactional(readOnly = true)
    public Page<MaterialSummaryDTO> getAllMaterials(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return materialRepository.findAll(pageable)
                .map(materialMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public Page<MaterialSummaryDTO> getActiveMaterials(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return materialRepository.findAllByIsActive(true, pageable)
                .map(materialMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public Page<MaterialSummaryDTO> getInactiveMaterials(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return materialRepository.findAllByIsActive(false, pageable)
                .map(materialMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public MaterialResponseDTO getMaterialById(Long materialId) {
        Material material = getMaterialOrThrow(materialId);
        return materialMapper.toDto(material);
    }

    @Transactional
    public MaterialResponseDTO createMaterial(CreateMaterialRequest request) {
        validateCreateMaterial(request);

        if (materialRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new ConflictException("Un matériau avec ce nom existe déjà.");
        }

        Material material = Material.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Material savedMaterial = materialRepository.save(material);

        MaterialPrice price = MaterialPrice.builder()
                .material(savedMaterial)
                .citizenPricePerKg(request.getCitizenPricePerKg())
                .collectorPricePerKg(request.getCollectorPricePerKg())
                .startDate(request.getStartDate() != null ? request.getStartDate() : LocalDateTime.now())
                .endDate(request.getEndDate())
                .build();

        materialPriceRepository.save(price);

        savedMaterial.setMaterialPrice(price);

        return materialMapper.toDto(savedMaterial);
    }

    @Transactional
    public MaterialResponseDTO updateMaterial(Long materialId, UpdateMaterialRequest request) {
        Material material = getMaterialOrThrow(materialId);

        if (request.getName() != null && !request.getName().isBlank()) {
            String newName = request.getName().trim();

            materialRepository.findByNameIgnoreCase(newName)
                    .filter(existing -> !existing.getId().equals(materialId))
                    .ifPresent(existing -> {
                        throw new ConflictException("Un matériau avec ce nom existe déjà.");
                    });

            material.setName(newName);
        }

        if (request.getDescription() != null) {
            material.setDescription(request.getDescription());
        }

        if (request.getIsActive() != null) {
            material.setIsActive(request.getIsActive());
        }

        return materialMapper.toDto(materialRepository.save(material));
    }

    @Transactional
    public MaterialResponseDTO updateMaterialPrice(Long materialId, UpdateMaterialPriceRequest request) {
        Material material = getMaterialOrThrow(materialId);

        validateUpdatePrice(request);

        MaterialPrice price = materialPriceRepository.lockByMaterialId(materialId)
                .orElse(null);

        if (price == null) {
            price = MaterialPrice.builder()
                    .material(material)
                    .citizenPricePerKg(request.getCitizenPricePerKg())
                    .collectorPricePerKg(request.getCollectorPricePerKg())
                    .startDate(request.getStartDate() != null ? request.getStartDate() : LocalDateTime.now())
                    .endDate(request.getEndDate())
                    .build();
        } else {
            price.setCitizenPricePerKg(request.getCitizenPricePerKg());
            price.setCollectorPricePerKg(request.getCollectorPricePerKg());

            if (request.getStartDate() != null) {
                price.setStartDate(request.getStartDate());
            }

            price.setEndDate(request.getEndDate());
        }

        MaterialPrice savedPrice = materialPriceRepository.save(price);
        material.setMaterialPrice(savedPrice);

        return materialMapper.toDto(material);
    }

    @Transactional
    public MaterialResponseDTO activateMaterial(Long materialId) {
        Material material = getMaterialOrThrow(materialId);
        material.activate();

        return materialMapper.toDto(materialRepository.save(material));
    }

    @Transactional
    public MaterialResponseDTO deactivateMaterial(Long materialId) {
        Material material = getMaterialOrThrow(materialId);
        material.deactivate();

        return materialMapper.toDto(materialRepository.save(material));
    }

    @Transactional(readOnly = true)
    public PricingStatsResponse getPricingStats() {
        return PricingStatsResponse.builder()
                .totalMaterials(materialRepository.count())
                .activeMaterials(materialRepository.countByIsActive(true))
                .inactiveMaterials(materialRepository.countByIsActive(false))
                .configuredPrices(materialPriceRepository.count())
                .build();
    }

    private Material getMaterialOrThrow(Long materialId) {
        return materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Matériau introuvable avec id : " + materialId));
    }

    private void validateCreateMaterial(CreateMaterialRequest request) {
        if (request == null) {
            throw new BadRequestException("La requête est obligatoire.");
        }

        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("Le nom du matériau est obligatoire.");
        }

        validatePriceValues(request.getCitizenPricePerKg(), request.getCollectorPricePerKg());
    }

    private void validateUpdatePrice(UpdateMaterialPriceRequest request) {
        if (request == null) {
            throw new BadRequestException("La requête est obligatoire.");
        }

        validatePriceValues(request.getCitizenPricePerKg(), request.getCollectorPricePerKg());
    }

    private void validatePriceValues(BigDecimal citizenPrice, BigDecimal collectorPrice) {
        if (citizenPrice == null || citizenPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Le prix citoyen doit être supérieur à zéro.");
        }

        if (collectorPrice == null || collectorPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Le prix collecteur doit être supérieur à zéro.");
        }

        if (collectorPrice.compareTo(citizenPrice) < 0) {
            throw new BadRequestException("Le prix collecteur doit être supérieur ou égal au prix citoyen.");
        }
    }

    @Data
    public static class CreateMaterialRequest {
        private String name;
        private String description;
        private Boolean isActive;

        private BigDecimal citizenPricePerKg;
        private BigDecimal collectorPricePerKg;

        private LocalDateTime startDate;
        private LocalDateTime endDate;
    }

    @Data
    public static class UpdateMaterialRequest {
        private String name;
        private String description;
        private Boolean isActive;
    }

    @Data
    public static class UpdateMaterialPriceRequest {
        private BigDecimal citizenPricePerKg;
        private BigDecimal collectorPricePerKg;

        private LocalDateTime startDate;
        private LocalDateTime endDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PricingStatsResponse {
        private Long totalMaterials;
        private Long activeMaterials;
        private Long inactiveMaterials;
        private Long configuredPrices;
    }

    public String getLastPriceUpdateDate() {
        return materialPriceRepository.findTopByOrderByCreatedAtDesc()
                .map(price -> price.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .orElse("Aucune mise à jour");
    }



    @Transactional
    public void deleteMaterial(Long materialId) {
        // 1. Vérifier l'existence du matériau
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Matériau introuvable avec l'ID : " + materialId));

        // 2. Vérifier qu'il n'y a pas de demandes de collecte associées
        if (material.getCollectionRequests() != null && !material.getCollectionRequests().isEmpty()) {
            throw new BadRequestException("Impossible de supprimer ce matériau car il est utilisé dans des demandes de collecte.");
        }

        // 3. Supprimer le prix associé (OneToOne)
        if (material.getMaterialPrice() != null) {
            materialPriceRepository.delete(material.getMaterialPrice());
        }

        // 4. Supprimer le matériau
        materialRepository.delete(material);
    }

    @Transactional(readOnly = true)
    public Page<MaterialSummaryDTO> getFilteredMaterials(
            String keyword,
            BigDecimal minCitizenPrice,
            BigDecimal maxCitizenPrice,
            BigDecimal minCollectorPrice,
            BigDecimal maxCollectorPrice,
            Pageable pageable) {

        Specification<Material> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtre par nom ou description
            if (keyword != null && !keyword.isBlank()) {
                String likePattern = "%" + keyword.toLowerCase() + "%";
                Predicate namePredicate = cb.like(cb.lower(root.get("name")), likePattern);
                Predicate descPredicate = cb.like(cb.lower(root.get("description")), likePattern);
                predicates.add(cb.or(namePredicate, descPredicate));
            }

            // Jointure avec MaterialPrice
            Join<Material, MaterialPrice> priceJoin = root.join("materialPrice", JoinType.LEFT);

            // Filtre prix citoyen
            if (minCitizenPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(priceJoin.get("citizenPricePerKg"), minCitizenPrice));
            }
            if (maxCitizenPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(priceJoin.get("citizenPricePerKg"), maxCitizenPrice));
            }

            // Filtre prix collecteur
            if (minCollectorPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(priceJoin.get("collectorPricePerKg"), minCollectorPrice));
            }
            if (maxCollectorPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(priceJoin.get("collectorPricePerKg"), maxCollectorPrice));
            }

            // Ne pas retourner les matériaux sans prix ? Ou les inclure quand même ?
            // On choisit de les inclure, les comparaisons avec null sont ignorées.

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return materialRepository.findAll(spec, pageable).map(materialMapper::toSummaryDto);
    }
}