// service/client/ClientWithdrawalService.java
package com.recyclix.backend.service.client;

import com.recyclix.backend.dto.transaction.WithdrawalRequestDTO;
import com.recyclix.backend.dto.transaction.TransactionResponseDTO;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.exception.UnauthorizedException;
import com.recyclix.backend.mapper.TransactionMapper;
import com.recyclix.backend.model.Account;
import com.recyclix.backend.model.Transaction;
import com.recyclix.backend.model.Wallet;
import com.recyclix.backend.repository.AccountRepository;
import com.recyclix.backend.repository.TransactionRepository;
import com.recyclix.backend.repository.WalletRepository;
import com.recyclix.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientWithdrawalService {

    private final AccountRepository accountRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    public TransactionResponseDTO requestWithdrawal(WithdrawalRequestDTO request) {
        // 1. Récupérer le client authentifié
        Account account = getAuthenticatedClientAccount();

        // 2. Verrouiller le wallet pour éviter les concurrences
        Wallet wallet = walletRepository.lockByAccountId(account.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet introuvable"));

        // 3. Vérifier que le solde est suffisant
        BigDecimal currentBalance = wallet.getBalanceMoney() == null ? BigDecimal.ZERO : wallet.getBalanceMoney();
        if (currentBalance.compareTo(request.getAmount()) < 0) {
            throw new BadRequestException("Solde insuffisant. Disponible : " + currentBalance + " DA");
        }

        // 4. Créer la transaction (statut PENDING, type CITIZEN_PAYMENT)
        Transaction transaction = Transaction.builder()
                .account(account)
                .type(Transaction.TransactionType.CITIZEN_PAYMENT)
                .amount(request.getAmount())
                .status(Transaction.TransactionStatus.PENDING)
                .accountHolderName(request.getAccountHolderName())
                .iban(request.getIban())
                .bankName(request.getBankName())
                .build();

        Transaction saved = transactionRepository.save(transaction);

        return transactionMapper.toResponseDTO(saved);
    }

    private Account getAuthenticatedClientAccount() {
        String email = SecurityUtils.getEmail()
                .orElseThrow(() -> new UnauthorizedException("Utilisateur non authentifié"));
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable"));
        if (account.getRoleType() != Account.RoleType.CLIENT) {
            throw new UnauthorizedException("Accès réservé aux clients");
        }
        return account;
    }
}