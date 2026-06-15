package com.recyclix.backend.util;

import com.recyclix.backend.security.AccountPrincipal;
import org.springframework.security.core.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static Optional<AccountPrincipal> getPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return Optional.empty();
        Object principal = auth.getPrincipal();
        return (principal instanceof AccountPrincipal ap) ? Optional.of(ap) : Optional.empty();
    }

    public static Optional<Long> getAccountId() {
        return getPrincipal().map(AccountPrincipal::getId);
    }

    public static Optional<String> getEmail() {
        return getPrincipal().map(AccountPrincipal::getEmail);
    }

    public static boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        String expected = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return auth.getAuthorities().stream().anyMatch(a -> expected.equals(a.getAuthority()));
    }

    public static boolean isClient() {
        return hasRole("CLIENT");
    }

    public static boolean isCollector() {
        return hasRole("COLLECTOR");
    }

    public static boolean isFactoryUser() {
        return hasRole("FACTORY_USER");
    }
}