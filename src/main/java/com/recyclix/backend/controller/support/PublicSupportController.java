package com.recyclix.backend.controller.support;

import com.recyclix.backend.dto.support.AnonymousTicketRequestDTO;
import com.recyclix.backend.service.support.AnonymousTicketService;
import com.recyclix.backend.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.recyclix.backend.exception.GlobalExceptionHandler.log;

@RestController
@RequestMapping("/api/public/support")
@RequiredArgsConstructor
public class PublicSupportController {

    private final AnonymousTicketService anonymousTicketService;

    @PostMapping("/ticket")
    public ApiResponse<Void> createTicket(@Valid @RequestBody AnonymousTicketRequestDTO request) {
        log.info("DTO reçu dans le contrôleur : email={}, roleType={}", request.getEmail(), request.getRoleType());
        anonymousTicketService.createResetPasswordTicket(request);
        return ApiResponse.okMessage("Votre demande a été transmise au support. Vous serez contacté rapidement.");
    }

    @PostMapping("/disabled-account-ticket")
    public ApiResponse<Void> createDisabledAccountTicket(@Valid @RequestBody AnonymousTicketRequestDTO request) {
        log.info("DTO reçu pour compte désactivé : email={}, roleType={}", request.getEmail(), request.getRoleType());
        anonymousTicketService.createDisabledAccountTicket(request);
        return ApiResponse.okMessage("Votre demande a été transmise au support. Vous serez contacté rapidement.");
    }
}