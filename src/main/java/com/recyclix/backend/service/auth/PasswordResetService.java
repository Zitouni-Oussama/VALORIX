//package com.recyclix.backend.service.auth;
//
//import com.recyclix.backend.dto.auth.ForgotPasswordRequestDTO;
//import com.recyclix.backend.dto.auth.ResetPasswordRequestDTO;
//import com.recyclix.backend.exception.BadRequestException;
//import com.recyclix.backend.exception.ResourceNotFoundException;
//import com.recyclix.backend.model.Account;
//import com.recyclix.backend.model.PasswordResetToken;
//import com.recyclix.backend.repository.AccountRepository;
//import com.recyclix.backend.repository.PasswordResetTokenRepository;
//import com.recyclix.backend.service.support.AnonymousTicketService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.security.SecureRandom;
//import java.time.LocalDateTime;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class PasswordResetService {
//
//    private final AccountRepository accountRepository;
//    private final PasswordResetTokenRepository tokenRepository;
//    private final EmailService emailService;
//    private final PasswordEncoder passwordEncoder;
//    private final AnonymousTicketService anonymousTicketService;
//
//    private static final SecureRandom random = new SecureRandom();
//    private static final int CODE_LENGTH = 8;
//    private static final int TOKEN_VALIDITY_MINUTES = 30;
//
//    @Transactional
//    public void requestPasswordReset(ForgotPasswordRequestDTO request) {
//        // 1. Chercher le compte par email
//        Account account = accountRepository.findByEmail(request.getEmail()).orElse(null);
//
//        if (account != null) {
//            // Cas 1 : Email trouvé → générer code et envoyer email
//            String code = generateResetCode();
//
//            // Supprimer les anciens tokens non utilisés de ce compte
//            tokenRepository.findByToken(code).ifPresent(tokenRepository::delete);
//
//            PasswordResetToken token = PasswordResetToken.builder()
//                    .token(code)
//                    .account(account)
//                    .expiryDate(LocalDateTime.now().plusMinutes(TOKEN_VALIDITY_MINUTES))
//                    .used(false)
//                    .build();
//
//            tokenRepository.save(token);
//
//            // Envoyer l'email avec le code
//            String userFullName = getAccountFullName(account);
//            emailService.sendResetCode(account.getEmail(), code, userFullName);
//
//            log.info("Code de réinitialisation envoyé à {}", account.getEmail());
//        } else {
//            // Cas 2 : Email non trouvé → créer ticket support anonyme
//            anonymousTicketService.createResetPasswordTicket(
//                    request.getEmail(),
//                    request.getFullName(),
//                    request.getPhone()
//            );
//            log.info("Ticket support créé pour email inconnu: {}", request.getEmail());
//        }
//    }
//
//    @Transactional
//    public void resetPassword(ResetPasswordRequestDTO request) {
//        // 1. Vérifier que les mots de passe correspondent
//        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
//            throw new BadRequestException("Les mots de passe ne correspondent pas.");
//        }
//
//        // 2. Chercher le token
//        PasswordResetToken token = tokenRepository.findByToken(request.getCode())
//                .orElseThrow(() -> new BadRequestException("Code invalide."));
//
//        // 3. Vérifier que le token est valide
//        if (!token.isValid()) {
//            throw new BadRequestException("Ce code a expiré ou a déjà été utilisé.");
//        }
//
//        // 4. Mettre à jour le mot de passe
//        Account account = token.getAccount();
//        account.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
//        accountRepository.save(account);
//
//        // 5. Marquer le token comme utilisé
//        token.setUsed(true);
//        tokenRepository.save(token);
//
//        log.info("Mot de passe réinitialisé pour le compte {}", account.getEmail());
//    }
//
//    private String generateResetCode() {
//        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ0123456789";
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < CODE_LENGTH; i++) {
//            sb.append(chars.charAt(random.nextInt(chars.length())));
//        }
//        return sb.toString();
//    }
//
//    private String getAccountFullName(Account account) {
//        if (account.getClient() != null) {
//            return account.getClient().getFirstName() + " " + account.getClient().getLastName();
//        } else if (account.getCollector() != null) {
//            return account.getCollector().getFirstName() + " " + account.getCollector().getLastName();
//        } else if (account.getFactoryUser() != null) {
//            return account.getFactoryUser().getFirstName() + " " + account.getFactoryUser().getLastName();
//        }
//        return account.getEmail().split("@")[0];
//    }
//}









package com.recyclix.backend.service.auth;

import com.recyclix.backend.dto.auth.ForgotPasswordRequestDTO;
import com.recyclix.backend.dto.auth.ResetPasswordRequestDTO;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.model.Account;
import com.recyclix.backend.model.PasswordResetToken;
import com.recyclix.backend.repository.AccountRepository;
import com.recyclix.backend.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final AccountRepository accountRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    private static final SecureRandom random = new SecureRandom();
    private static final int CODE_LENGTH = 8;
    private static final int TOKEN_VALIDITY_MINUTES = 30;

    /**
     * Vérifie l'email et envoie un code s'il existe.
     * @return true si l'email existe et le code a été envoyé, false sinon
     */
    @Transactional
    public boolean requestPasswordReset(ForgotPasswordRequestDTO request) {
        Account account = accountRepository.findByEmail(request.getEmail()).orElse(null);
        if (account == null) {
            log.warn("Email non trouvé: {}", request.getEmail());
            return false;
        }

        String code = generateResetCode();
        // Supprimer les anciens tokens non utilisés de ce compte
        tokenRepository.findByToken(code).ifPresent(tokenRepository::delete);

        PasswordResetToken token = PasswordResetToken.builder()
                .token(code)
                .account(account)
                .expiryDate(LocalDateTime.now().plusMinutes(TOKEN_VALIDITY_MINUTES))
                .used(false)
                .build();

        tokenRepository.save(token);

        String userFullName = getAccountFullName(account);
        emailService.sendResetCode(account.getEmail(), code, userFullName);

        log.info("Code de réinitialisation envoyé à {}", account.getEmail());
        return true;
    }

    @Transactional
    public void resetPassword(ResetPasswordRequestDTO request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Les mots de passe ne correspondent pas.");
        }

        PasswordResetToken token = tokenRepository.findByToken(request.getCode())
                .orElseThrow(() -> new BadRequestException("Code invalide."));

        if (!token.isValid()) {
            throw new BadRequestException("Ce code a expiré ou a déjà été utilisé.");
        }

        Account account = token.getAccount();
        account.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);

        token.setUsed(true);
        tokenRepository.save(token);

        log.info("Mot de passe réinitialisé pour {}", account.getEmail());
    }

    private String generateResetCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String getAccountFullName(Account account) {
        if (account.getClient() != null) {
            return account.getClient().getFirstName() + " " + account.getClient().getLastName();
        } else if (account.getCollector() != null) {
            return account.getCollector().getFirstName() + " " + account.getCollector().getLastName();
        } else if (account.getFactoryUser() != null) {
            return account.getFactoryUser().getFirstName() + " " + account.getFactoryUser().getLastName();
        }
        return account.getEmail().split("@")[0];
    }
}