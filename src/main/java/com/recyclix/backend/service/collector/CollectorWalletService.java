package com.recyclix.backend.service.collector;

import com.recyclix.backend.dto.payment.PaymentResponseDTO;
import com.recyclix.backend.dto.transaction.TransactionResponseDTO;
import com.recyclix.backend.dto.wallet.WalletResponseDTO;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.exception.UnauthorizedException;
import com.recyclix.backend.mapper.PaymentMapper;
import com.recyclix.backend.mapper.TransactionMapper;
import com.recyclix.backend.mapper.WalletMapper;
import com.recyclix.backend.model.Account;
import com.recyclix.backend.model.Wallet;
import com.recyclix.backend.repository.AccountRepository;
import com.recyclix.backend.repository.PaymentRepository;
import com.recyclix.backend.repository.TransactionRepository;
import com.recyclix.backend.repository.WalletRepository;
import com.recyclix.backend.util.SecurityUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CollectorWalletService {

    private final AccountRepository accountRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentRepository paymentRepository;

    private final WalletMapper walletMapper;
    private final TransactionMapper transactionMapper;
    private final PaymentMapper paymentMapper;

    //. -------------------- GET MY WALLET OVERVIEW -------------------- .\\
    @Transactional(readOnly = true)
    public CollectorWalletOverviewResponse getMyWalletOverview() {
        Account account = getAuthenticatedCollectorAccount();
        Wallet wallet = getWalletByAccount(account);

        WalletResponseDTO walletDto = walletMapper.toResponseDTO(wallet);

        // compléter le champ transactions
        walletDto.setTransactions(
                transactionRepository.countByAccountId(account.getId()).intValue()
        );

        return CollectorWalletOverviewResponse.builder()
                .wallet(walletDto)
                .transactions(getMyTransactions())
                .payments(getMyPayments())
                .build();
    }

    //. -------------------- GET MY WALLET ONLY -------------------- .\\
    @Transactional(readOnly = true)
    public WalletResponseDTO getMyWallet() {
        Account account = getAuthenticatedCollectorAccount();
        Wallet wallet = getWalletByAccount(account);

        WalletResponseDTO dto = walletMapper.toResponseDTO(wallet);
        dto.setTransactions(
                transactionRepository.countByAccountId(account.getId()).intValue()
        );

        return dto;
    }

    //. -------------------- GET MY TRANSACTIONS -------------------- .\\
    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getMyTransactions() {
        Account account = getAuthenticatedCollectorAccount();

        return transactionRepository.findAllByAccountIdOrderByCreatedAtDesc(account.getId())
                .stream()
                .map(transactionMapper::toResponseDTO)
                .toList();
    }

    //. -------------------- GET MY PAYMENTS -------------------- .\\
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getMyPayments() {
        Account account = getAuthenticatedCollectorAccount();

        return paymentRepository.findAllByAccountIdOrderByPaymentDateDesc(account.getId())
                .stream()
                .map(paymentMapper::toResponseDTO)
                .toList();
    }

    //* =========================================================
    //! HELPERS
    //* =========================================================
    private Account getAuthenticatedCollectorAccount() {
        String email = SecurityUtils.getEmail()
                .orElseThrow(() -> new UnauthorizedException("Utilisateur non authentifié."));

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable."));

        if (account.getRoleType() != Account.RoleType.COLLECTOR) {
            throw new UnauthorizedException("Accès réservé au collecteur.");
        }

        return account;
    }

    private Wallet getWalletByAccount(Account account) {
        return walletRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet introuvable pour ce compte."));
    }

    // =========================================================
    // RESPONSE DTO INTERNE
    // =========================================================
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CollectorWalletOverviewResponse {
        private WalletResponseDTO wallet;
        private List<TransactionResponseDTO> transactions;
        private List<PaymentResponseDTO> payments;
    }
}