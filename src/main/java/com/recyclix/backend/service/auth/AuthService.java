package com.recyclix.backend.service.auth;

import com.recyclix.backend.dto.account.AccountRequestDTO;
import com.recyclix.backend.dto.account.AccountResponseDTO;
import com.recyclix.backend.dto.auth.AuthResponseDTO;
import com.recyclix.backend.dto.auth.LoginRequestDTO;
import com.recyclix.backend.dto.client.ClientRequestDTO;
import com.recyclix.backend.dto.collector.CollectorRequestDTO;
import com.recyclix.backend.dto.factory_user.FactoryUserRequestDTO;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ConflictException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.exception.UnauthorizedException;
import com.recyclix.backend.mapper.AccountMapper;
import com.recyclix.backend.model.Account;
import com.recyclix.backend.model.Client;
import com.recyclix.backend.model.Collector;
import com.recyclix.backend.model.FactoryUser;
import com.recyclix.backend.model.Wallet;
import com.recyclix.backend.repository.AccountRepository;
import com.recyclix.backend.repository.ClientRepository;
import com.recyclix.backend.repository.CollectorRepository;
import com.recyclix.backend.repository.FactoryUserRepository;
import com.recyclix.backend.repository.WalletRepository;
import com.recyclix.backend.security.AccountPrincipal;
import com.recyclix.backend.security.JwtService;
import com.recyclix.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;
    private final CollectorRepository collectorRepository;
    private final FactoryUserRepository factoryUserRepository;
    private final WalletRepository walletRepository;

    private final AccountMapper accountMapper;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    //. -------------------- REGISTER CLIENT -------------------- .\\
    public AuthResponseDTO registerClient(AccountRequestDTO accountDto, ClientRequestDTO clientDto) {
        validateRegistrationInputs(accountDto);
        validateClientProfileForRegister(clientDto);

        if (accountDto.getRoleType() != Account.RoleType.CLIENT) {
            throw new BadRequestException("Le rôle du compte doit être CLIENT.");
        }

        ensureEmailAvailable(accountDto.getEmail());

        Account account = buildAccountForRegistration(accountDto);
        account = accountRepository.save(account);

        clientDto.setAccountId(account.getId());

        Client client = Client.builder()
                .account(account)
                .firstName(clientDto.getFirstName())
                .lastName(clientDto.getLastName())
                .address(clientDto.getAddress())
                .latitude(clientDto.getLatitude())
                .longitude(clientDto.getLongitude())
                .totalPoints(0)
                .build();
        client = clientRepository.save(client);

        Wallet wallet = createWalletForAccount(account);

        Account persisted = accountRepository.findById(account.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable après inscription."));

        String token = jwtService.generateToken(AccountPrincipal.from(persisted));

        return AuthResponseDTO.builder()
                .token(token)
                .account(accountMapper.toDto(persisted))
                .clientId(client.getId())
                .walletId(wallet.getId())
                .build();
    }

    //. -------------------- REGISTER COLLECTOR -------------------- .\\
    public AuthResponseDTO registerCollector(AccountRequestDTO accountDto, CollectorRequestDTO collectorDto) {
        validateRegistrationInputs(accountDto);
        validateCollectorProfileForRegister(collectorDto);

        if (accountDto.getRoleType() != Account.RoleType.COLLECTOR) {
            throw new BadRequestException("Le rôle du compte doit être COLLECTOR.");
        }

        ensureEmailAvailable(accountDto.getEmail());

        Account account = buildAccountForRegistration(accountDto);
        account = accountRepository.save(account);

        collectorDto.setAccountId(account.getId());

        Collector collector = Collector.builder()
                .account(account)
                .firstName(collectorDto.getFirstName())
                .lastName(collectorDto.getLastName())
                .nationalIdNumber(collectorDto.getNationalIdNumber())
                .isVerified(false)
                .averageRating(BigDecimal.ZERO)
                .currentLatitude(collectorDto.getCurrentLatitude())
                .currentLongitude(collectorDto.getCurrentLongitude())
                .build();
        collector = collectorRepository.save(collector);

        Wallet wallet = createWalletForAccount(account);

        Account persisted = accountRepository.findById(account.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable après inscription."));

        String token = jwtService.generateToken(AccountPrincipal.from(persisted));

        return AuthResponseDTO.builder()
                .token(token)
                .account(accountMapper.toDto(persisted))
                .collectorId(collector.getId())
                .walletId(wallet.getId())
                .build();
    }

    //. -------------------- REGISTER FACTORY USER -------------------- .\\
    public AuthResponseDTO registerFactoryUser(AccountRequestDTO accountDto, FactoryUserRequestDTO factoryUserDto) {
        validateRegistrationInputs(accountDto);
        validateFactoryUserProfileForRegister(factoryUserDto);

        if (accountDto.getRoleType() != Account.RoleType.FACTORY_USER) {
            throw new BadRequestException("Le rôle du compte doit être FACTORY_USER.");
        }

        ensureEmailAvailable(accountDto.getEmail());

        Account account = buildAccountForRegistration(accountDto);
        account = accountRepository.save(account);

        factoryUserDto.setAccountId(account.getId());

        FactoryUser factoryUser = FactoryUser.builder()
                .account(account)
                .firstName(factoryUserDto.getFirstName())
                .lastName(factoryUserDto.getLastName())
                .employeeNumber(factoryUserDto.getEmployeeNumber())
                .position(factoryUserDto.getPosition())
                .build();
        factoryUser = factoryUserRepository.save(factoryUser);

        Wallet wallet = createWalletForAccount(account);

        Account persisted = accountRepository.findById(account.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable après inscription."));

        String token = jwtService.generateToken(AccountPrincipal.from(persisted));

        return AuthResponseDTO.builder()
                .token(token)
                .account(accountMapper.toDto(persisted))
                .factoryUserId(factoryUser.getId())
                .walletId(wallet.getId())
                .build();
    }

    //. -------------------- LOGIN -------------------- .\\
    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO dto) {
        Account account = accountRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Email invalide."));

        if (account.getStatus() == Account.AccountStatus.INACTIVE ||
                account.getStatus() == Account.AccountStatus.DELETED) {
            throw new UnauthorizedException("Ce compte n'est pas autorisé à se connecter.");
        }

        if (!passwordEncoder.matches(dto.getPassword(), account.getPasswordHash())) {
            throw new UnauthorizedException("Mot de passe invalide.");
        }

        String token = jwtService.generateToken(AccountPrincipal.from(account));

        System.out.println("Role = " + account.getRoleType());
        System.out.println("Client = " + account.getClient());
        System.out.println("Collector = " + account.getCollector());
        System.out.println("FactoryUser = " + account.getFactoryUser());

        return AuthResponseDTO.builder()
                .token(token)
                .account(accountMapper.toDto(account))
                .clientId(account.getClient() != null ? account.getClient().getId() : null)
                .collectorId(account.getCollector() != null ? account.getCollector().getId() : null)
                .factoryUserId(account.getFactoryUser() != null ? account.getFactoryUser().getId() : null)
                .walletId(account.getWallet() != null ? account.getWallet().getId() : null)
                .build();
    }

    //. -------------------- ME -------------------- .\\
    @Transactional(readOnly = true)
    public AccountResponseDTO me() {
        String email = SecurityUtils.getEmail()
                .orElseThrow(() -> new UnauthorizedException("Utilisateur non authentifié."));

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable."));

        return accountMapper.toDto(account);
    }

    //* =========================================================
    //! HELPERS
    //* =========================================================
    private void validateRegistrationInputs(AccountRequestDTO accountDto) {
        if (accountDto == null) {
            throw new BadRequestException("Les informations du compte sont obligatoires.");
        }

        if (accountDto.getPassword() == null || accountDto.getPassword().isBlank()) {
            throw new BadRequestException("Le mot de passe est obligatoire.");
        }

        if (accountDto.getRoleType() == null) {
            throw new BadRequestException("Le rôle du compte est obligatoire.");
        }
    }
    private void ensureEmailAvailable(String email) {
        if (accountRepository.findByEmail(email).isPresent()) {
            throw new ConflictException("Un compte avec cet email existe déjà.");
        }
    }
    private Account buildAccountForRegistration(AccountRequestDTO dto) {
        Account account = accountMapper.toEntity(dto);

        account.setPasswordHash(passwordEncoder.encode(dto.getPassword()));

        if (account.getStatus() == null) {
            account.setStatus(Account.AccountStatus.ACTIVE);
        }

        return account;
    }

    private Wallet createWalletForAccount(Account account) {
        Wallet wallet = Wallet.builder()
                .account(account)
                .balanceMoney(BigDecimal.ZERO)
                .balancePoints(0)
                .build();

        return walletRepository.save(wallet);
    }

    private void validateClientProfileForRegister(ClientRequestDTO clientDto) {
        if (clientDto == null) {
            throw new BadRequestException("Les informations du client sont obligatoires.");
        }

        if (clientDto.getFirstName() == null || clientDto.getFirstName().isBlank()) {
            throw new BadRequestException("Le prénom du client est obligatoire.");
        }

        if (clientDto.getLastName() == null || clientDto.getLastName().isBlank()) {
            throw new BadRequestException("Le nom du client est obligatoire.");
        }
    }

    private void validateCollectorProfileForRegister(CollectorRequestDTO collectorDto) {
        if (collectorDto == null) {
            throw new BadRequestException("Les informations du collecteur sont obligatoires.");
        }

        if (collectorDto.getFirstName() == null || collectorDto.getFirstName().isBlank()) {
            throw new BadRequestException("Le prénom du collecteur est obligatoire.");
        }

        if (collectorDto.getLastName() == null || collectorDto.getLastName().isBlank()) {
            throw new BadRequestException("Le nom du collecteur est obligatoire.");
        }

        if (collectorDto.getNationalIdNumber() == null || collectorDto.getNationalIdNumber().isBlank()) {
            throw new BadRequestException("Le numéro d'identité nationale est obligatoire.");
        }
    }

    private void validateFactoryUserProfileForRegister(FactoryUserRequestDTO factoryUserDto) {
        if (factoryUserDto == null) {
            throw new BadRequestException("Les informations de l'employé usine sont obligatoires.");
        }

        if (factoryUserDto.getFirstName() == null || factoryUserDto.getFirstName().isBlank()) {
            throw new BadRequestException("Le prénom est obligatoire.");
        }

        if (factoryUserDto.getLastName() == null || factoryUserDto.getLastName().isBlank()) {
            throw new BadRequestException("Le nom est obligatoire.");
        }

        if (factoryUserDto.getEmployeeNumber() == null || factoryUserDto.getEmployeeNumber().isBlank()) {
            throw new BadRequestException("Le numéro employé est obligatoire.");
        }

        if (factoryUserDto.getPosition() == null) {
            throw new BadRequestException("Le poste est obligatoire.");
        }
    }
}