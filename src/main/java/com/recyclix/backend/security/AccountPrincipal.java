package com.recyclix.backend.security;

import com.recyclix.backend.model.Account;
import lombok.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@AllArgsConstructor
public class AccountPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final String role;
    private final String status;

    public static AccountPrincipal from(Account a) {
        String role = (a.getRoleType() != null) ? a.getRoleType().name() : "CLIENT";
        String status = (a.getStatus() != null) ? a.getStatus().name() : "ACTIVE";

        return new AccountPrincipal(
                a.getId(),
                a.getEmail(),
                a.getPasswordHash(),
                role,
                status
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    // ✅ Ici on bloque INACTIVE + DELETED
    @Override
    public boolean isEnabled() {
        return !"DELETED".equalsIgnoreCase(status) && !"INACTIVE".equalsIgnoreCase(status);
    }

    @Override
    public boolean isAccountNonLocked() {
        return isEnabled();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}