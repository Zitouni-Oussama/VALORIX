package com.recyclix.backend.controller.auth;

import com.recyclix.backend.dto.account.AccountRequestDTO;
import com.recyclix.backend.dto.account.AccountResponseDTO;
import com.recyclix.backend.dto.auth.AuthResponseDTO;
import com.recyclix.backend.dto.auth.LoginRequestDTO;
import com.recyclix.backend.dto.client.ClientRequestDTO;
import com.recyclix.backend.dto.collector.CollectorRequestDTO;
import com.recyclix.backend.dto.factory_user.FactoryUserRequestDTO;
import com.recyclix.backend.service.auth.AuthService;
import com.recyclix.backend.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // =========================================================
    // REGISTER CLIENT
    // =========================================================
    @PostMapping("/register/client")
    public ApiResponse<AuthResponseDTO> registerClient(
            @Valid @RequestBody RegisterClientRequest request
    ) {
        AuthResponseDTO response = authService.registerClient(
                request.getAccount(),
                request.getClient()
        );

        return ApiResponse.ok("Inscription client réussie.", response);
    }

    // =========================================================
    // REGISTER COLLECTOR
    // =========================================================
    @PostMapping("/register/collector")
    public ApiResponse<AuthResponseDTO> registerCollector(
            @Valid @RequestBody RegisterCollectorRequest request
    ) {
        AuthResponseDTO response = authService.registerCollector(
                request.getAccount(),
                request.getCollector()
        );

        return ApiResponse.ok("Inscription collecteur réussie.", response);
    }

    // =========================================================
    // REGISTER FACTORY USER
    // =========================================================
    @PostMapping("/register/factory-user")
    public ApiResponse<AuthResponseDTO> registerFactoryUser(
            @Valid @RequestBody RegisterFactoryUserRequest request
    ) {
        AuthResponseDTO response = authService.registerFactoryUser(
                request.getAccount(),
                request.getFactoryUser()
        );

        return ApiResponse.ok("Inscription employé usine réussie.", response);
    }

    // =========================================================
    // LOGIN
    // =========================================================
    @PostMapping("/login")
    public ApiResponse<AuthResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request
    ) {
        AuthResponseDTO response = authService.login(request);
        return ApiResponse.ok("Connexion réussie.", response);
    }

    // =========================================================
    // ME
    // =========================================================
    @GetMapping("/me")
    public ApiResponse<AccountResponseDTO> me() {
        AccountResponseDTO response = authService.me();
        return ApiResponse.ok("Profil récupéré avec succès.", response);
    }

    // =========================================================
    // REQUEST WRAPPERS
    // =========================================================
    @lombok.Data
    public static class RegisterClientRequest {
        @Valid
        private AccountRequestDTO account;

        private ClientRequestDTO client;
    }

    @lombok.Data
    public static class RegisterCollectorRequest {
        @Valid
        private AccountRequestDTO account;

        private CollectorRequestDTO collector;
    }

    @lombok.Data
    public static class RegisterFactoryUserRequest {
        @Valid
        private AccountRequestDTO account;

        private FactoryUserRequestDTO factoryUser;
    }
}