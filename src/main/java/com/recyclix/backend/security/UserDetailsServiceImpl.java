package com.recyclix.backend.security;

import com.recyclix.backend.model.Account;
import com.recyclix.backend.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Account acc = accountRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Account introuvable: " + email));
        return AccountPrincipal.from(acc);
    }
}