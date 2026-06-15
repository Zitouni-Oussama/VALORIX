package com.recyclix.backend.controller.auth;

import com.recyclix.backend.dto.auth.AdminResetPasswordDTO;
import com.recyclix.backend.dto.auth.ChangePasswordRequestDTO;
import com.recyclix.backend.service.auth.PasswordService;
import com.recyclix.backend.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/password")
@RequiredArgsConstructor
public class PasswordController {

    private final PasswordService passwordService;

    // =========================================================
    // CHANGE MY PASSWORD
    // =========================================================
    @PutMapping("/change")
    public ApiResponse<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequestDTO request
    ) {
        passwordService.changePassword(request);
        return ApiResponse.okMessage("Mot de passe modifié avec succès.");
    }

    // =========================================================
    // ADMIN RESET PASSWORD
    // =========================================================
    @PutMapping("/admin/reset")
    @PreAuthorize("hasRole('FACTORY_USER')")
    public ApiResponse<Void> adminResetPassword(
            @Valid @RequestBody AdminResetPasswordDTO request
    ) {
        passwordService.adminResetPassword(request);
        return ApiResponse.okMessage("Mot de passe réinitialisé avec succès.");
    }
}