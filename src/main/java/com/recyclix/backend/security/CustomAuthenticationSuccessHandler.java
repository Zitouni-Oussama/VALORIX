package com.recyclix.backend.security;

import com.recyclix.backend.model.FactoryUser;
import com.recyclix.backend.repository.FactoryUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final FactoryUserRepository factoryUserRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        AccountPrincipal principal = (AccountPrincipal) authentication.getPrincipal();
        String role = principal.getRole();

        // Si l'utilisateur est un FACTORY_USER, vérifier sa position
        if ("FACTORY_USER".equals(role)) {
            Long accountId = principal.getId();
            FactoryUser factoryUser = factoryUserRepository.findByAccountId(accountId).orElse(null);
            if (factoryUser != null && factoryUser.getPosition() == FactoryUser.FactoryPosition.ACCOUNTANT) {
                response.sendRedirect("/accountant/dashboard");
                return;
            }
        }

        // Par défaut, rediriger vers l'admin
        response.sendRedirect("/admin/dashboard");
    }
}