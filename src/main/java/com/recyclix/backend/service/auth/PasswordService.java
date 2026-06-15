package com.recyclix.backend.service.auth;

import com.recyclix.backend.dto.auth.AdminResetPasswordDTO;
import com.recyclix.backend.dto.auth.ChangePasswordRequestDTO;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.exception.UnauthorizedException;
import com.recyclix.backend.model.Account;
import com.recyclix.backend.repository.AccountRepository;
import com.recyclix.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PasswordService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    //. -------------------- CHANGE MY PASSWORD -------------------- .\\
    public void changePassword(ChangePasswordRequestDTO dto) {
        String email = SecurityUtils.getEmail()
                .orElseThrow(() -> new UnauthorizedException("Utilisateur non authentifié."));

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable."));

        validateChangePasswordDto(dto);

        if (!passwordEncoder.matches(dto.getCurrentPassword(), account.getPasswordHash())) {
            throw new UnauthorizedException("Le mot de passe actuel est incorrect.");
        }

        if (passwordEncoder.matches(dto.getNewPassword(), account.getPasswordHash())) {
            throw new BadRequestException("Le nouveau mot de passe doit être différent de l'ancien.");
        }

        account.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        accountRepository.save(account);
    }

    //. -------------------- ADMIN RESET PASSWORD -------------------- .\\
    // =========================================================
    public void adminResetPassword(AdminResetPasswordDTO dto) {
        validateAdminResetDto(dto);

        Account account = accountRepository.findById(dto.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable."));

        account.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        accountRepository.save(account);
    }

    //* =========================================================
    //! HELPERS
    //* =========================================================
    private void validateChangePasswordDto(ChangePasswordRequestDTO dto) {
        if (dto == null) {
            throw new BadRequestException("Les données de changement de mot de passe sont obligatoires.");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BadRequestException("La confirmation du nouveau mot de passe ne correspond pas.");
        }
    }

    private void validateAdminResetDto(AdminResetPasswordDTO dto) {
        if (dto == null) {
            throw new BadRequestException("Les données de réinitialisation sont obligatoires.");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BadRequestException("La confirmation du nouveau mot de passe ne correspond pas.");
        }
    }
}