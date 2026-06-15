package com.recyclix.backend.service.workshop;

import com.recyclix.backend.dto.workshop.*;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.exception.UnauthorizedException;
import com.recyclix.backend.mapper.MaterialStockMapper;
import com.recyclix.backend.mapper.TreatmentBatchMapper;
import com.recyclix.backend.model.*;
import com.recyclix.backend.repository.*;
import com.recyclix.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TreatmentService {

    private final MaterialStockRepository materialStockRepository;
    private final TreatmentBatchRepository treatmentBatchRepository;
    private final MaterialRepository materialRepository;
    private final FactoryUserRepository factoryUserRepository;

    private final MaterialStockMapper materialStockMapper;
    private final TreatmentBatchMapper treatmentBatchMapper;

    // ==================== STOCK ====================

    @Transactional(readOnly = true)
    public List<MaterialStockDTO> getAllMaterialStocks() {
        return materialStockRepository.findAll().stream()
                .map(materialStockMapper::toDto)
                .toList();
    }

    // ==================== TREATMENT BATCHES ====================

    public TreatmentBatchDTO startTreatment(StartTreatmentRequest request) {
        FactoryUser currentUser = getCurrentFactoryUser();

        Material material = materialRepository.findById(request.getMaterialId())
                .orElseThrow(() -> new ResourceNotFoundException("Matériau introuvable"));

        // Vérifier le stock disponible
        MaterialStock stock = materialStockRepository.findByMaterialId(material.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Stock introuvable pour ce matériau"));

        if (stock.getQuantityKg().compareTo(request.getQuantityKg()) < 0) {
            throw new BadRequestException("Stock insuffisant. Disponible: " + stock.getQuantityKg() + " kg");
        }

        // Déduire du stock
        stock.setQuantityKg(stock.getQuantityKg().subtract(request.getQuantityKg()));
        materialStockRepository.save(stock);

        // Créer le lot
        String batchNumber = generateBatchNumber();
        TreatmentBatch batch = TreatmentBatch.builder()
                .batchNumber(batchNumber)
                .material(material)
                .initialQuantityKg(request.getQuantityKg())
                .status(TreatmentBatch.BatchStatus.PROCESSING)
                .startedBy(currentUser)
                .notes(request.getNotes())
                .build();

        TreatmentBatch saved = treatmentBatchRepository.save(batch);
        return treatmentBatchMapper.toDto(saved);
    }

    public TreatmentBatchDTO completeTreatment(Long batchId, CompleteTreatmentRequest request) {
        FactoryUser currentUser = getCurrentFactoryUser();

        TreatmentBatch batch = treatmentBatchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Lot introuvable"));

        if (batch.getStatus() != TreatmentBatch.BatchStatus.PROCESSING) {
            throw new BadRequestException("Seul un lot en cours de traitement peut être terminé");
        }

        batch.setProcessedQuantityKg(request.getProcessedQuantityKg());
        batch.setStatus(TreatmentBatch.BatchStatus.COMPLETED);
        batch.setCompletedAt(LocalDateTime.now());
        batch.setCompletedBy(currentUser);
        if (request.getNotes() != null) batch.setNotes(request.getNotes());

        TreatmentBatch saved = treatmentBatchRepository.save(batch);
        return treatmentBatchMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<TreatmentBatchDTO> getProcessingHistory(String status, Long materialId) {
        List<TreatmentBatch> batches;
        if (status != null && materialId != null) {
            batches = treatmentBatchRepository.findByStatusAndMaterialId(
                    TreatmentBatch.BatchStatus.valueOf(status.toUpperCase()), materialId);
        } else if (status != null) {
            batches = treatmentBatchRepository.findByStatus(TreatmentBatch.BatchStatus.valueOf(status.toUpperCase()));
        } else if (materialId != null) {
            batches = treatmentBatchRepository.findByMaterialId(materialId);
        } else {
            batches = treatmentBatchRepository.findAllByOrderByStartedAtDesc();
        }
        return batches.stream().map(treatmentBatchMapper::toDto).toList();
    }

    // ==================== HELPERS ====================

    private FactoryUser getCurrentFactoryUser() {
        Long accountId = SecurityUtils.getAccountId()
                .orElseThrow(() -> new UnauthorizedException("Utilisateur non authentifié"));
        return factoryUserRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Employé usine introuvable"));
    }

    private String generateBatchNumber() {
        return "BATCH-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    @Transactional
    public TreatmentBatchDTO updateTreatmentBatch(Long batchId, UpdateTreatmentBatchRequest request) {
        TreatmentBatch batch = treatmentBatchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Lot introuvable"));
        if (batch.getStatus() != TreatmentBatch.BatchStatus.PROCESSING) {
            throw new BadRequestException("Seul un lot en cours de traitement peut être modifié");
        }
        BigDecimal oldQuantity = batch.getInitialQuantityKg();
        BigDecimal newQuantity = request.getInitialQuantityKg();
        if (newQuantity != null && !newQuantity.equals(oldQuantity)) {
            // Mettre à jour le stock : restaurer l'ancienne quantité et déduire la nouvelle
            MaterialStock stock = materialStockRepository.findByMaterialId(batch.getMaterial().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Stock introuvable"));
            stock.setQuantityKg(stock.getQuantityKg().add(oldQuantity).subtract(newQuantity));
            materialStockRepository.save(stock);
            batch.setInitialQuantityKg(newQuantity);
        }
        if (request.getNotes() != null) {
            batch.setNotes(request.getNotes());
        }
        TreatmentBatch saved = treatmentBatchRepository.save(batch);
        return treatmentBatchMapper.toDto(saved);
    }

    @Transactional
    public void deleteTreatmentBatch(Long batchId) {
        TreatmentBatch batch = treatmentBatchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Lot introuvable"));
        if (batch.getStatus() != TreatmentBatch.BatchStatus.PROCESSING) {
            throw new BadRequestException("Seul un lot en cours de traitement peut être supprimé");
        }
        // Restaurer le stock
        MaterialStock stock = materialStockRepository.findByMaterialId(batch.getMaterial().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Stock introuvable"));
        stock.setQuantityKg(stock.getQuantityKg().add(batch.getInitialQuantityKg()));
        materialStockRepository.save(stock);
        treatmentBatchRepository.delete(batch);
    }
}