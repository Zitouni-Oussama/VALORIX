package com.recyclix.backend.security;

import com.recyclix.backend.exception.ForbiddenException;
import com.recyclix.backend.model.Account;
import com.recyclix.backend.model.FactoryUser;
import com.recyclix.backend.repository.AccountRepository;
import com.recyclix.backend.repository.FactoryUserRepository;
import com.recyclix.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("factoryAccess")
@RequiredArgsConstructor
public class FactoryAccessService {

    private final AccountRepository accountRepository;
    private final FactoryUserRepository factoryUserRepository;

    public boolean hasPosition(FactoryUser.FactoryPosition requiredPosition){

        if (!SecurityUtils.isFactoryUser()) {
            return false;
        }

        Long accountId = SecurityUtils.getAccountId()
                .orElseThrow(() -> new ForbiddenException("Utilisateur non authentifié"));

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ForbiddenException("Compte introuvable"));

        FactoryUser factoryUser = factoryUserRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new ForbiddenException("Profil usine introuvable"));

        return factoryUser.getPosition() != null &&
                factoryUser.getPosition() == requiredPosition;
    }
}