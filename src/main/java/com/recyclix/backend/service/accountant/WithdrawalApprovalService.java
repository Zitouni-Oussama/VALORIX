// service/accountant/WithdrawalApprovalService.java
package com.recyclix.backend.service.accountant;

import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.model.*;
import com.recyclix.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WithdrawalApprovalService {

    private static final BigDecimal POINT_TO_DA_RATE = new BigDecimal("0.5");

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final PaymentRepository paymentRepository;
    private final ExpenseRepository expenseRepository;
    private final FactoryUserRepository factoryUserRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public void approveWithdrawal(Long transactionId, String note, Long currentFactoryUserId) {
        // 1. Récupérer la transaction
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction introuvable"));

        // 2. Vérifier que c'est bien une demande de retrait en attente
        if (transaction.getType() != Transaction.TransactionType.CITIZEN_PAYMENT) {
            throw new BadRequestException("Cette transaction n'est pas une demande de retrait");
        }
        if (transaction.getStatus() != Transaction.TransactionStatus.PENDING) {
            throw new BadRequestException("Cette transaction n'est plus en attente");
        }

        Account account = transaction.getAccount();
        if (account == null) {
            throw new ResourceNotFoundException("Compte client introuvable");
        }

        // 3. Vérifier les points et l'argent disponibles
        Wallet wallet = walletRepository.lockByAccountId(account.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet introuvable"));

        BigDecimal amount = transaction.getAmount();
        // Calcul des points nécessaires (1 point = 0.5 DA)
        int pointsRequired = amount.divide(POINT_TO_DA_RATE, 0, RoundingMode.CEILING).intValue();

        Integer currentPoints = wallet.getBalancePoints() == null ? 0 : wallet.getBalancePoints();
        BigDecimal currentMoney = wallet.getBalanceMoney() == null ? BigDecimal.ZERO : wallet.getBalanceMoney();

        if (currentPoints < pointsRequired) {
            throw new BadRequestException("Points insuffisants. Client a " + currentPoints + " points, nécessite " + pointsRequired);
        }
        if (currentMoney.compareTo(amount) < 0) {
            throw new BadRequestException("Solde argent insuffisant. Client a " + currentMoney + " DA, demande " + amount);
        }

        // 4. Mettre à jour le wallet
        wallet.setBalancePoints(currentPoints - pointsRequired);
        wallet.setBalanceMoney(currentMoney.subtract(amount));
        walletRepository.save(wallet);

        // 5. Créer le Payment
        Payment payment = Payment.builder()
                .account(account)
                .amount(amount)
                .paymentMethod(Payment.PaymentMethod.BANK_TRANSFER)
                .status(Payment.PaymentStatus.COMPLETED)
                .description(note != null ? note : "Retrait approuvé")
                .accountHolderName(transaction.getAccountHolderName())
                .iban(transaction.getIban())
                .bankName(transaction.getBankName())
                .build();
        paymentRepository.save(payment);

        // 6. Créer la Dépense (Expense) pour la comptabilité
        FactoryUser currentUser = factoryUserRepository.findByAccountId(currentFactoryUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Comptable non trouvé"));

        Expense expense = Expense.builder()
                .category(Expense.ExpenseCategory.CLIENT_PAYMENT) // ou une catégorie dédiée "CASH_WITHDRAWAL"
                .description("Retrait client : " + account.getEmail() + " - " + amount + " DA")
                .amount(amount)
                .expenseDate(LocalDate.now())
                .createdBy(currentUser)
                .build();
        expenseRepository.save(expense);

        // 7. Mettre à jour la transaction
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transactionRepository.save(transaction);
    }

    @Transactional
    public void rejectWithdrawal(Long transactionId, String reason) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction introuvable"));

        if (transaction.getType() != Transaction.TransactionType.CITIZEN_PAYMENT) {
            throw new BadRequestException("Cette transaction n'est pas une demande de retrait");
        }
        if (transaction.getStatus() != Transaction.TransactionStatus.PENDING) {
            throw new BadRequestException("Cette transaction n'est plus en attente");
        }

        transaction.setStatus(Transaction.TransactionStatus.CANCELLED);
        transaction.setDescription(reason);
        transactionRepository.save(transaction);
    }
}