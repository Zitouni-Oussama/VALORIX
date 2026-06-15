package com.recyclix.backend.service.accountant;

import com.recyclix.backend.dto.factory_invoice.FactoryInvoiceRequestDTO;
import com.recyclix.backend.dto.factory_invoice.FactoryInvoiceResponseDTO;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.mapper.FactoryInvoiceMapper;
import com.recyclix.backend.model.FactoryInvoice;
import com.recyclix.backend.repository.FactoryInvoiceRepository;
import com.recyclix.backend.util.SecurityUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FactoryInvoiceService {

    private final FactoryInvoiceRepository invoiceRepository;
    private final FactoryInvoiceMapper invoiceMapper;

    // Compteur séquentiel pour les numéros de facture (à remplacer par séquence BDD en prod)
    private static final AtomicLong SEQUENCE = new AtomicLong(1);

    // =========================================================
    // CREATE
    // =========================================================

    public FactoryInvoiceResponseDTO createInvoice(FactoryInvoiceRequestDTO request) {
        validateRequest(request);

        // Création de l'entité
        FactoryInvoice invoice = invoiceMapper.toEntity(request);

        // Définir la date d'émission
        invoice.setIssueDate(request.getIssueDate() != null ? request.getIssueDate() : LocalDate.now());

        // Définir le statut par défaut
        if (request.getStatus() == null) {
            invoice.setStatus(FactoryInvoice.InvoiceStatus.DRAFT);
        }

        // Calcul des montants TVA/TTC
        invoice.calculateAmounts();

        // Génération du numéro de facture unique
        String referenceNumber = generateUniqueReferenceNumber();
        invoice.setReferenceNumber(referenceNumber);

        // Ajouter l'utilisateur qui a créé
        SecurityUtils.getAccountId().ifPresent(invoice::setCreatedBy);

        FactoryInvoice saved = invoiceRepository.save(invoice);
        log.info("Facture créée : {} pour {}", referenceNumber, invoice.getFactoryName());

        return invoiceMapper.toResponseDTO(saved);
    }

    // =========================================================
    // READ
    // =========================================================

    @Transactional(readOnly = true)
    public Page<FactoryInvoiceResponseDTO> getAllInvoices(Pageable pageable) {
        return invoiceRepository.findAll(pageable).map(invoiceMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public FactoryInvoiceResponseDTO getInvoiceById(Long id) {
        FactoryInvoice invoice = getInvoiceOrThrow(id);
        return invoiceMapper.toResponseDTO(invoice);
    }

    @Transactional(readOnly = true)
    public List<FactoryInvoiceResponseDTO> getInvoicesByStatus(FactoryInvoice.InvoiceStatus status) {
        return invoiceRepository.findByStatus(status).stream()
                .map(invoiceMapper::toResponseDTO)
                .toList();
    }

    // =========================================================
    // UPDATE
    // =========================================================

    public FactoryInvoiceResponseDTO updateInvoice(Long id, FactoryInvoiceRequestDTO request) {
        FactoryInvoice invoice = getInvoiceOrThrow(id);

        // Une facture payée ou annulée ne peut pas être modifiée
        if (invoice.getStatus() == FactoryInvoice.InvoiceStatus.PAID ||
                invoice.getStatus() == FactoryInvoice.InvoiceStatus.CANCELLED) {
            throw new BadRequestException("Une facture payée ou annulée ne peut pas être modifiée.");
        }

        // Mise à jour des champs
        if (request.getFactoryName() != null) invoice.setFactoryName(request.getFactoryName());
        if (request.getFactoryAddress() != null) invoice.setFactoryAddress(request.getFactoryAddress());
        if (request.getFactoryTaxId() != null) invoice.setFactoryTaxId(request.getFactoryTaxId());
        if (request.getAmountHt() != null) invoice.setAmountHt(request.getAmountHt());
        if (request.getTvaRate() != null) invoice.setTvaRate(request.getTvaRate());
        if (request.getDueDate() != null) invoice.setDueDate(request.getDueDate());
        if (request.getPaymentTerms() != null) invoice.setPaymentTerms(request.getPaymentTerms());
        if (request.getNotes() != null) invoice.setNotes(request.getNotes());
        if (request.getBankIban() != null) invoice.setBankIban(request.getBankIban());
        if (request.getBankBic() != null) invoice.setBankBic(request.getBankBic());
        if (request.getTotalWeightKg() != null) invoice.setTotalWeightKg(request.getTotalWeightKg());
        if (request.getCollectionsCount() != null) invoice.setCollectionsCount(request.getCollectionsCount());

        // Recalcul automatique
        invoice.calculateAmounts();

        FactoryInvoice updated = invoiceRepository.save(invoice);
        log.info("Facture mise à jour : {}", invoice.getReferenceNumber());

        return invoiceMapper.toResponseDTO(updated);
    }

    // =========================================================
    // WORKFLOW
    // =========================================================

    @Transactional
    public FactoryInvoiceResponseDTO markAsPaid(Long id) {

        FactoryInvoice invoice = getInvoiceOrThrow(id);

        invoice.setStatus(FactoryInvoice.InvoiceStatus.PAID);

        if (invoice.getIssueDate() == null) {
            invoice.setIssueDate(LocalDate.now());
        }

        invoice.calculateAmounts();

        FactoryInvoice saved = invoiceRepository.save(invoice);

        return invoiceMapper.toResponseDTO(saved);
    }

    public FactoryInvoiceResponseDTO markAsOverdue(Long id) {
        FactoryInvoice invoice = getInvoiceOrThrow(id);

        if (invoice.getStatus() == FactoryInvoice.InvoiceStatus.PAID) {
            throw new BadRequestException("Une facture payée ne peut pas être en retard.");
        }

        invoice.markAsOverdue();
        FactoryInvoice saved = invoiceRepository.save(invoice);

        return invoiceMapper.toResponseDTO(saved);
    }

    public FactoryInvoiceResponseDTO cancelInvoice(Long id) {
        FactoryInvoice invoice = getInvoiceOrThrow(id);

        if (invoice.getStatus() == FactoryInvoice.InvoiceStatus.PAID) {
            throw new BadRequestException("Une facture payée ne peut pas être annulée.");
        }

        invoice.setStatus(FactoryInvoice.InvoiceStatus.CANCELLED);
        FactoryInvoice saved = invoiceRepository.save(invoice);
        log.info("Facture annulée : {}", invoice.getReferenceNumber());

        return invoiceMapper.toResponseDTO(saved);
    }

    // =========================================================
    // DELETE
    // =========================================================

    public void deleteInvoice(Long id) {
        FactoryInvoice invoice = getInvoiceOrThrow(id);

        if (invoice.getStatus() == FactoryInvoice.InvoiceStatus.PAID) {
            throw new BadRequestException("Une facture payée ne peut pas être supprimée.");
        }

        invoiceRepository.delete(invoice);
        log.info("Facture supprimée : {}", invoice.getReferenceNumber());
    }

    // =========================================================
    // STATISTIQUES
    // =========================================================

    @Transactional(readOnly = true)
    public InvoiceStatistics getStatistics() {
        List<FactoryInvoice> allInvoices = invoiceRepository.findAll();

        BigDecimal totalHt = BigDecimal.ZERO;
        BigDecimal totalTva = BigDecimal.ZERO;
        BigDecimal totalTtc = BigDecimal.ZERO;

        for (FactoryInvoice inv : allInvoices) {
            if (inv.getStatus() == FactoryInvoice.InvoiceStatus.PAID) {
                totalHt = totalHt.add(inv.getAmountHt() != null ? inv.getAmountHt() : BigDecimal.ZERO);
                totalTva = totalTva.add(inv.getTvaAmount() != null ? inv.getTvaAmount() : BigDecimal.ZERO);
                totalTtc = totalTtc.add(inv.getAmountTtc() != null ? inv.getAmountTtc() : BigDecimal.ZERO);
            }
        }

        return InvoiceStatistics.builder()
                .totalInvoices((long) allInvoices.size())
                .paidInvoices(allInvoices.stream().filter(i -> i.getStatus() == FactoryInvoice.InvoiceStatus.PAID).count())
                .pendingInvoices(allInvoices.stream().filter(i -> i.getStatus() == FactoryInvoice.InvoiceStatus.PENDING).count())
                .overdueInvoices(allInvoices.stream().filter(i -> i.getStatus() == FactoryInvoice.InvoiceStatus.OVERDUE).count())
                .totalAmountHt(totalHt)
                .totalAmountTva(totalTva)
                .totalAmountTtc(totalTtc)
                .build();
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private FactoryInvoice getInvoiceOrThrow(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Facture introuvable avec ID : " + id));
    }

    private void validateRequest(FactoryInvoiceRequestDTO request) {
        if (request.getAmountHt() == null || request.getAmountHt().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Le montant HT doit être supérieur à zéro.");
        }

        if (request.getTvaRate() == null) {
            throw new BadRequestException("Le taux de TVA est obligatoire.");
        }

        if (request.getTvaRate().compareTo(BigDecimal.ZERO) < 0 ||
                request.getTvaRate().compareTo(new BigDecimal("0.30")) > 0) {
            throw new BadRequestException("Le taux TVA doit être entre 0% et 30%.");
        }
    }

    private String generateUniqueReferenceNumber() {
        LocalDate now = LocalDate.now();
        String prefix = String.format("FACT-%04d%02d%02d-",
                now.getYear(), now.getMonthValue(), now.getDayOfMonth());

        // En production, utilisez une séquence BDD plutôt qu'un compteur mémoire
        long sequence = SEQUENCE.getAndIncrement();

        return prefix + String.format("%06d", sequence);
    }

    @Transactional(readOnly = true)
    public Page<FactoryInvoiceResponseDTO> getFilteredInvoices(
            String status,
            String factoryName,
            BigDecimal minAmountHt,
            BigDecimal maxAmountHt,
            LocalDate startIssueDate,
            LocalDate endIssueDate,
            Pageable pageable) {

        Specification<FactoryInvoice> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null && !status.isBlank()) {
                try {
                    FactoryInvoice.InvoiceStatus statusEnum = FactoryInvoice.InvoiceStatus.valueOf(status.toUpperCase());
                    predicates.add(cb.equal(root.get("status"), statusEnum));
                } catch (IllegalArgumentException ignored) {}
            }

            if (factoryName != null && !factoryName.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("factoryName")), "%" + factoryName.toLowerCase() + "%"));
            }

            if (minAmountHt != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amountHt"), minAmountHt));
            }
            if (maxAmountHt != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amountHt"), maxAmountHt));
            }

            if (startIssueDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("issueDate"), startIssueDate));
            }
            if (endIssueDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("issueDate"), endIssueDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return invoiceRepository.findAll(spec, pageable).map(invoiceMapper::toResponseDTO);
    }

    // =========================================================
    // STATS DTO
    // =========================================================

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class InvoiceStatistics {
        private Long totalInvoices;
        private Long paidInvoices;
        private Long pendingInvoices;
        private Long overdueInvoices;
        private BigDecimal totalAmountHt;
        private BigDecimal totalAmountTva;
        private BigDecimal totalAmountTtc;
    }
}