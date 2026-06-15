package com.recyclix.backend.service.accountant;

import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.model.Payment;
import com.recyclix.backend.model.Transaction;
import com.recyclix.backend.model.Wallet;
import com.recyclix.backend.repository.PaymentRepository;
import com.recyclix.backend.repository.TransactionRepository;
import com.recyclix.backend.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class TreasuryAndWithdrawalService {

    private final WalletRepository walletRepository;
    private final PaymentRepository paymentRepository;
    private final TransactionRepository transactionRepository;

    private static final BigDecimal POINT_TO_DA_RATE = new BigDecimal("0.5");

    public TreasuryAndWithdrawalService(
            WalletRepository walletRepository,
            PaymentRepository paymentRepository,
            TransactionRepository transactionRepository
    ) {
        this.walletRepository = walletRepository;
        this.paymentRepository = paymentRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public TreasurySummary getTreasurySummary() {
        Long totalPoints = walletRepository.sumClientPoints();

        if (totalPoints == null) {
            totalPoints = 0L;
        }

        BigDecimal totalLiabilityDA = BigDecimal.valueOf(totalPoints)
                .multiply(POINT_TO_DA_RATE)
                .setScale(3, RoundingMode.HALF_UP);

        return new TreasurySummary(totalPoints, totalLiabilityDA);
    }

    // Dans TreasuryAndWithdrawalService.java
    public List<Payment> getPendingWithdrawals() {
        return paymentRepository.findByStatusWithAccount(Payment.PaymentStatus.PENDING);
    }

    @Transactional
    public void approveWithdrawal(Long paymentId) {
        if (paymentId == null) {
            throw new BadRequestException("L'identifiant du paiement est obligatoire.");
        }

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement introuvable."));

        if (payment.getStatus() != Payment.PaymentStatus.PENDING) {
            throw new BadRequestException("Ce paiement n'est pas en attente.");
        }

        if (payment.getAccount() == null) {
            throw new ResourceNotFoundException("Ce paiement n'est lié à aucun compte.");
        }

        BigDecimal amountDA = payment.getAmount();

        if (amountDA == null || amountDA.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Le montant du retrait est invalide.");
        }

        Wallet wallet = walletRepository.lockByAccountId(payment.getAccount().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet introuvable pour ce citoyen."));

        int pointsToDeduct = amountDA
                .divide(POINT_TO_DA_RATE, 0, RoundingMode.CEILING)
                .intValue();

        Integer currentPoints = wallet.getBalancePoints() == null
                ? 0
                : wallet.getBalancePoints();

        if (currentPoints < pointsToDeduct) {
            throw new BadRequestException("Le citoyen n'a pas assez de points.");
        }

        wallet.setBalancePoints(currentPoints - pointsToDeduct);

        BigDecimal currentMoney = wallet.getBalanceMoney() == null
                ? BigDecimal.ZERO
                : wallet.getBalanceMoney();

//        if (currentMoney.compareTo(amountDA) >= 0) {
//            wallet.setBalanceMoney(
//                    currentMoney.subtract(amountDA).setScale(3, RoundingMode.HALF_UP)
//            );
//        }

        if (currentMoney.compareTo(amountDA) < 0) {
            throw new BadRequestException("Le citoyen n'a pas assez d'argent dans son wallet.");
        }

        wallet.setBalanceMoney(
                currentMoney.subtract(amountDA).setScale(3, RoundingMode.HALF_UP)
        );

        walletRepository.save(wallet);

        payment.complete();
        paymentRepository.save(payment);

        Transaction transaction = Transaction.builder()
                .account(payment.getAccount())
                .status(Transaction.TransactionStatus.COMPLETED)
                .amount(amountDA)
                .type(Transaction.TransactionType.CITIZEN_PAYMENT)
                .collection(null)
                .build();

        transactionRepository.save(transaction);
    }

    @Transactional
    public void rejectWithdrawal(Long paymentId, String reason) {
        if (paymentId == null) {
            throw new BadRequestException("L'identifiant du paiement est obligatoire.");
        }

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement introuvable."));

        if (payment.getStatus() != Payment.PaymentStatus.PENDING) {
            throw new BadRequestException("Seul un paiement en attente peut être rejeté.");
        }

        payment.fail();
        payment.setDescription(reason);
        paymentRepository.save(payment);
    }

    public static class TreasurySummary {
        public Long totalPointsInCirculation;
        public BigDecimal totalLiabilityDA;

        public TreasurySummary(Long points, BigDecimal da) {
            this.totalPointsInCirculation = points;
            this.totalLiabilityDA = da;
        }
    }
}