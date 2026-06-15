//package com.recyclix.backend.controller.auth;
//
//import com.recyclix.backend.dto.auth.ForgotPasswordRequestDTO;
//import com.recyclix.backend.dto.auth.ResetPasswordRequestDTO;
//import com.recyclix.backend.service.auth.PasswordResetService;
//import com.recyclix.backend.util.ApiResponse;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/auth")
//@RequiredArgsConstructor
//public class PasswordResetController {
//
//    private final PasswordResetService passwordResetService;
//
//    @PostMapping("/forgot-password")
//    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
//        passwordResetService.requestPasswordReset(request);
//        // Toujours retourner le même message pour des raisons de sécurité
//        return ApiResponse.okMessage("Si un compte existe avec cet email, vous recevrez un code de réinitialisation par email.");
//    }
//
//    @PostMapping("/reset-password")
//    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
//        passwordResetService.resetPassword(request);
//        return ApiResponse.okMessage("Votre mot de passe a été réinitialisé avec succès. Vous pouvez maintenant vous connecter.");
//    }
//}



















package com.recyclix.backend.controller.auth;

import com.recyclix.backend.dto.auth.ForgotPasswordRequestDTO;
import com.recyclix.backend.dto.auth.ResetPasswordRequestDTO;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.service.auth.PasswordResetService;
import com.recyclix.backend.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        boolean emailExists = passwordResetService.requestPasswordReset(request);
        if (emailExists) {
            return ResponseEntity.ok(ApiResponse.okMessage(
                    "Si un compte existe avec cet email, vous recevrez un code de réinitialisation."));
        } else {
            // Retourner une erreur 404 avec un code spécifique
            throw new ResourceNotFoundException("EMAIL_NOT_FOUND");
        }
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        passwordResetService.resetPassword(request);
        return ApiResponse.okMessage("Votre mot de passe a été réinitialisé avec succès. Vous pouvez maintenant vous connecter.");
    }
}