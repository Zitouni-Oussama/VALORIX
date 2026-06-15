package com.recyclix.backend.service.accountant;

import com.recyclix.backend.dto.collector_payment.UnpaidValidationDTO;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.model.*;
import com.recyclix.backend.repository.*;
import com.recyclix.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CollectorPaymentService {

    private final FactoryValidationRepository validationRepository;
    private final CollectorRepository collectorRepository;
    private final PaymentRepository paymentRepository;
    private final WalletRepository walletRepository;
    private final FactoryUserRepository factoryUserRepository;
    private final ExpenseRepository expenseRepository;  // ← AJOUT

    @Transactional(readOnly = true)
    public List<UnpaidValidationDTO> getUnpaidValidations(Long collectorId) {
        if (collectorId == null) {
            throw new BadRequestException("L'identifiant du collecteur est obligatoire.");
        }
        Collector collector = collectorRepository.findById(collectorId)
                .orElseThrow(() -> new ResourceNotFoundException("Collecteur introuvable."));

        List<FactoryValidation> validations = validationRepository.findUnpaidByCollector(collectorId);
        return validations.stream().map(this::toUnpaidDTO).toList();
    }

    @Transactional
    public byte[] payValidations(Long collectorId, List<Long> validationIds, Payment.PaymentMethod paymentMethod) {
        if (collectorId == null) {
            throw new BadRequestException("L'identifiant du collecteur est obligatoire.");
        }
        if (validationIds == null || validationIds.isEmpty()) {
            throw new BadRequestException("Aucune validation sélectionnée.");
        }

        Collector collector = collectorRepository.findById(collectorId)
                .orElseThrow(() -> new ResourceNotFoundException("Collecteur introuvable."));
        Account collectorAccount = collector.getAccount();
        if (collectorAccount == null) {
            throw new ResourceNotFoundException("Compte du collecteur introuvable.");
        }

        List<FactoryValidation> validations = validationRepository.findAllById(validationIds);
        for (FactoryValidation v : validations) {
            if (v.getPaid()) {
                throw new BadRequestException("La validation " + v.getId() + " a déjà été payée.");
            }
            if (v.getRejectionReason() != null) {
                throw new BadRequestException("La validation " + v.getId() + " est rejetée, impossible de la payer.");
            }
            FactoryDelivery delivery = v.getDelivery();
            if (delivery.getStatus() != FactoryDelivery.DeliveryStatus.VALIDATED &&
                    delivery.getStatus() != FactoryDelivery.DeliveryStatus.ADJUSTED) {
                throw new BadRequestException("La livraison associée n'est pas validée ou ajustée.");
            }
        }

        BigDecimal totalAmount = validations.stream()
                .map(FactoryValidation::getCollectorAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Le montant total à payer est nul ou négatif.");
        }

        // Récupération de l'utilisateur qui effectue le paiement (pour traçabilité)
        Long currentAccountId = SecurityUtils.getAccountId()
                .orElseThrow(() -> new BadRequestException("Utilisateur non authentifié."));
        FactoryUser payingUser = factoryUserRepository.findByAccountId(currentAccountId).orElse(null);

        // 1. Création du paiement (trace de la transaction)
        Payment payment = Payment.builder()
                .account(collectorAccount)
                .amount(totalAmount)
                .paymentMethod(paymentMethod)
                .status(Payment.PaymentStatus.COMPLETED)
                .description("Paiement usine pour livraisons validées")
                .paymentDate(LocalDateTime.now())
                .build();
        payment = paymentRepository.save(payment);

        // 2. Diminuer le wallet du collecteur (car il reçoit l'argent en physique)
        Wallet wallet = collectorAccount.getWallet();
        if (wallet == null) {
            wallet = walletRepository.findByAccountId(collectorAccount.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Wallet du collecteur introuvable."));
        }
        BigDecimal currentBalance = wallet.getBalanceMoney() == null ? BigDecimal.ZERO : wallet.getBalanceMoney();
        if (currentBalance.compareTo(totalAmount) < 0) {
            throw new BadRequestException("Solde insuffisant pour effectuer ce paiement. Disponible : " + currentBalance);
        }
        wallet.setBalanceMoney(currentBalance.subtract(totalAmount));
        walletRepository.save(wallet);

        // 3. Enregistrer une dépense pour l'usine (impact sur les revenus collectés)
        Expense expense = Expense.builder()
                .category(Expense.ExpenseCategory.COLLECTOR_PAYMENT)
                .description("Paiement collecteur " + collector.getFirstName() + " " + collector.getLastName())
                .amount(totalAmount)
                .expenseDate(LocalDate.now())
                .createdBy(payingUser)
                .build();
        expenseRepository.save(expense);
        log.info("Dépense enregistrée pour l'usine : {} DA pour paiement collecteur {}", totalAmount, collectorId);

        // 4. Marquer les validations comme payées
        for (FactoryValidation v : validations) {
            v.setPaid(true);
        }
        validationRepository.saveAll(validations);

        log.info("Paiement de {} DA effectué pour le collecteur {} (ID {}). Nouveau solde wallet : {}. Validations : {}",
                totalAmount, collector.getFirstName() + " " + collector.getLastName(), collectorId, wallet.getBalanceMoney(), validationIds);

        return generatePaymentReceiptPdf(collector, validations, payment, payingUser);
    }

    private UnpaidValidationDTO toUnpaidDTO(FactoryValidation v) {
        String materialName = null;
        if (v.getDelivery() != null && v.getDelivery().getCollection() != null
                && v.getDelivery().getCollection().getRequest() != null
                && v.getDelivery().getCollection().getRequest().getMaterial() != null) {
            materialName = v.getDelivery().getCollection().getRequest().getMaterial().getName();
        }
        return UnpaidValidationDTO.builder()
                .validationId(v.getId())
                .deliveryId(v.getDelivery() != null ? v.getDelivery().getId() : null)
                .collectionId(v.getDelivery() != null && v.getDelivery().getCollection() != null ?
                        v.getDelivery().getCollection().getId() : null)
                .validatedAt(v.getValidatedAt())
                .validatedWeight(v.getValidatedWeight())
                .collectorAmount(v.getCollectorAmount())
                .materialName(materialName)
                .build();
    }

    private byte[] generatePaymentReceiptPdf(Collector collector, List<FactoryValidation> validations,
                                             Payment payment, FactoryUser payingUser) {
        return new CollectorPaymentPdfService().generateReceipt(collector, validations, payment, payingUser);
    }
}