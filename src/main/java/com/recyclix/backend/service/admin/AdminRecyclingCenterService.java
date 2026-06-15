// java/com/recyclix/backend/service/admin/AdminRecyclingCenterService.java
package com.recyclix.backend.service.admin;

import com.recyclix.backend.dto.recycling_center.RecyclingCenterRequestDTO;
import com.recyclix.backend.dto.recycling_center.RecyclingCenterResponseDTO;
import com.recyclix.backend.dto.recycling_center.RecyclingCenterSummaryDTO;
import com.recyclix.backend.dto.recycling_center.RecyclingCenterUpdateDTO;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ConflictException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.mapper.RecyclingCenterMapper;
import com.recyclix.backend.model.FactoryDelivery;
import com.recyclix.backend.model.RecyclingCenter;
import com.recyclix.backend.repository.FactoryDeliveryRepository;
import com.recyclix.backend.repository.RecyclingCenterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminRecyclingCenterService {

    private final RecyclingCenterRepository centerRepository;
    private final FactoryDeliveryRepository deliveryRepository;
    private final RecyclingCenterMapper centerMapper;

    @Transactional(readOnly = true)
    public Page<RecyclingCenterSummaryDTO> getAllCenters(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return centerRepository.findAll(pageable).map(centerMapper::toSummaryDTO);
    }

    @Transactional(readOnly = true)
    public RecyclingCenterResponseDTO getCenterById(Long id) {
        RecyclingCenter center = getCenterOrThrow(id);
        return centerMapper.toResponseDTO(center);
    }

    @Transactional(readOnly = true)
    public RecyclingCenterStatsDTO getCenterStats(Long id) {
        RecyclingCenter center = getCenterOrThrow(id);
        List<FactoryDelivery> deliveries = deliveryRepository.findAll().stream()
                .filter(d -> d.getRecyclingCenter() != null && d.getRecyclingCenter().getId().equals(id))
                .collect(Collectors.toList());

        long total = deliveries.size();
        long validated = deliveries.stream().filter(d -> d.getStatus() == FactoryDelivery.DeliveryStatus.VALIDATED).count();
        long adjusted = deliveries.stream().filter(d -> d.getStatus() == FactoryDelivery.DeliveryStatus.ADJUSTED).count();
        long refused = deliveries.stream().filter(d -> d.getStatus() == FactoryDelivery.DeliveryStatus.REFUSED).count();
        long pending = deliveries.stream().filter(d -> d.getStatus() == FactoryDelivery.DeliveryStatus.PENDING).count();
        long processing = deliveries.stream().filter(d -> d.getStatus() == FactoryDelivery.DeliveryStatus.PROCESSING).count();
        long completed = deliveries.stream().filter(d -> d.getStatus() == FactoryDelivery.DeliveryStatus.COMPLETED).count();

        return RecyclingCenterStatsDTO.builder()
                .centerId(id)
                .centerName(center.getName())
                .totalDeliveries(total)
                .validatedDeliveries(validated)
                .adjustedDeliveries(adjusted)
                .refusedDeliveries(refused)
                .pendingDeliveries(pending)
                .processingDeliveries(processing)
                .completedDeliveries(completed)
                .build();
    }

    public RecyclingCenterResponseDTO createCenter(RecyclingCenterRequestDTO request) {
        validateCreateRequest(request);
        if (centerRepository.findByNameContainingIgnoreCase(request.getName(), PageRequest.of(0, 1)).hasContent()) {
            throw new ConflictException("Un centre avec ce nom existe déjà.");
        }
        RecyclingCenter center = centerMapper.toEntity(request);
        // ✅ Ajouter une capacité par défaut (par exemple 1000000 kg)
        if (center.getCapacity() == null) {
            center.setCapacity(java.math.BigDecimal.valueOf(1000000));
        }
        RecyclingCenter saved = centerRepository.save(center);
        return centerMapper.toResponseDTO(saved);
    }

    public RecyclingCenterResponseDTO updateCenter(Long id, RecyclingCenterUpdateDTO request) {
        RecyclingCenter center = getCenterOrThrow(id);
        centerMapper.updateEntityFromDTO(request, center);
        RecyclingCenter saved = centerRepository.save(center);
        return centerMapper.toResponseDTO(saved);
    }

    public void deleteCenter(Long id) {
        RecyclingCenter center = getCenterOrThrow(id);
        // Vérifier si des livraisons ou users sont liés
        boolean hasDeliveries = deliveryRepository.findAll().stream()
                .anyMatch(d -> d.getRecyclingCenter() != null && d.getRecyclingCenter().getId().equals(id));
        if (hasDeliveries) {
            throw new BadRequestException("Impossible de supprimer ce centre car il est associé à des livraisons.");
        }
        centerRepository.delete(center);
    }

    private RecyclingCenter getCenterOrThrow(Long id) {
        return centerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Centre de recyclage introuvable avec ID : " + id));
    }

    private void validateCreateRequest(RecyclingCenterRequestDTO request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("Le nom du centre est obligatoire.");
        }
        if (request.getAddress() == null || request.getAddress().isBlank()) {
            throw new BadRequestException("L'adresse est obligatoire.");
        }
    }

    // DTO pour les statistiques
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RecyclingCenterStatsDTO {
        private Long centerId;
        private String centerName;
        private Long totalDeliveries;
        private Long validatedDeliveries;
        private Long adjustedDeliveries;
        private Long refusedDeliveries;
        private Long pendingDeliveries;
        private Long processingDeliveries;
        private Long completedDeliveries;
    }

    @Transactional(readOnly = true)
    public Page<RecyclingCenterSummaryDTO> getFilteredCenters(
            String name, String location, String email, String phone,
            BigDecimal minCapacity, BigDecimal maxCapacity,
            int page, int size) {

        Specification<RecyclingCenter> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (location != null && !location.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%"));
            }
            // Filtre email (recherche dans contactInfo)
            if (email != null && !email.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("contactInfo")), "%email: " + email.toLowerCase() + "%"));
            }
            // Filtre téléphone (recherche dans contactInfo)
            if (phone != null && !phone.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("contactInfo")), "%phone: " + phone.toLowerCase() + "%"));
            }
            if (minCapacity != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("capacity"), minCapacity));
            }
            if (maxCapacity != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("capacity"), maxCapacity));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return centerRepository.findAll(spec, pageable).map(centerMapper::toSummaryDTO);
    }
}