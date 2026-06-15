package com.recyclix.backend.service.client;

import com.recyclix.backend.dto.material.ClientMaterialDetailDTO;
import com.recyclix.backend.dto.material.ClientMaterialSummaryDTO;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.model.Material;
import com.recyclix.backend.model.MaterialPrice;
import com.recyclix.backend.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientMaterialService {

    private final MaterialRepository materialRepository;

    public List<ClientMaterialSummaryDTO> getAvailableMaterials() {
        return materialRepository.findAll()
                .stream()
                .filter(material -> Boolean.TRUE.equals(material.getIsActive()))
                .sorted(Comparator.comparing(Material::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toSummaryDto)
                .toList();
    }

    public ClientMaterialDetailDTO getMaterialDetails(Long materialId) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Matériau introuvable."));

        if (!Boolean.TRUE.equals(material.getIsActive())) {
            throw new ResourceNotFoundException("Matériau introuvable.");
        }

        return toDetailDto(material);
    }

    private ClientMaterialSummaryDTO toSummaryDto(Material material) {
        MaterialPrice price = material.getMaterialPrice();

        return ClientMaterialSummaryDTO.builder()
                .id(material.getId())
                .name(material.getName())
                .isActive(material.getIsActive())
                .citizenPricePerKg(price != null ? price.getCitizenPricePerKg() : null)
                .collectorPricePerKg(price != null ? price.getCollectorPricePerKg() : null)
                .build();
    }

    private ClientMaterialDetailDTO toDetailDto(Material material) {
        MaterialPrice price = material.getMaterialPrice();

        return ClientMaterialDetailDTO.builder()
                .id(material.getId())
                .name(material.getName())
                .description(material.getDescription())
                .isActive(material.getIsActive())
                .createdAt(material.getCreatedAt())
                .citizenPricePerKg(price != null ? price.getCitizenPricePerKg() : null)
                .collectorPricePerKg(price != null ? price.getCollectorPricePerKg() : null)
                .priceStartDate(price != null ? price.getStartDate() : null)
                .priceEndDate(price != null ? price.getEndDate() : null)
                .build();
    }
}