package com.recyclix.backend.service.admin;

import com.recyclix.backend.dto.factory_user.FactoryUserResponseDTO;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ConflictException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.mapper.FactoryUserMapper;
import com.recyclix.backend.model.Account;
import com.recyclix.backend.model.FactoryUser;
import com.recyclix.backend.model.RecyclingCenter;
import com.recyclix.backend.model.Wallet;
import com.recyclix.backend.repository.AccountRepository;
import com.recyclix.backend.repository.FactoryUserRepository;
import com.recyclix.backend.repository.RecyclingCenterRepository;
import com.recyclix.backend.repository.WalletRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AdminFactoryUserService {

    private final AccountRepository accountRepository;
    private final FactoryUserRepository factoryUserRepository;
    private final WalletRepository walletRepository;
    private final FactoryUserMapper factoryUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final RecyclingCenterRepository recyclingCenterRepository;


    public FactoryUserResponseDTO createAccountant(CreateFactoryUserRequest request, Long recyclingCenterId) {
        return createFactoryUser(request, FactoryUser.FactoryPosition.ACCOUNTANT, recyclingCenterId);
    }

    public FactoryUserResponseDTO createWorkshopManager(CreateFactoryUserRequest request, Long recyclingCenterId) {
        return createFactoryUser(request, FactoryUser.FactoryPosition.MANAGER, recyclingCenterId);
    }

    @Transactional(readOnly = true)
    public Page<FactoryUserResponseDTO> getAllFactoryUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return factoryUserRepository.findAll(pageable)
                .map(factoryUserMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<FactoryUserResponseDTO> getFactoryUsersByPosition(
            FactoryUser.FactoryPosition position,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return factoryUserRepository.findAllByPosition(position, pageable)
                .map(factoryUserMapper::toDto);
    }

    @Transactional(readOnly = true)
    public FactoryUserResponseDTO getFactoryUserById(Long id) {
        FactoryUser factoryUser = getFactoryUserOrThrow(id);
        return factoryUserMapper.toDto(factoryUser);
    }

    @Transactional
    public FactoryUserResponseDTO updateFactoryUser(Long id, UpdateFactoryUserRequest request) {
        FactoryUser factoryUser = getFactoryUserOrThrow(id);

        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            factoryUser.setFirstName(request.getFirstName().trim());
        }

        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            factoryUser.setLastName(request.getLastName().trim());
        }

        if (request.getEmployeeNumber() != null && !request.getEmployeeNumber().isBlank()) {
            factoryUser.setEmployeeNumber(request.getEmployeeNumber().trim());
        }

        if (request.getPosition() != null) {
            factoryUser.setPosition(request.getPosition());
        }

        if (request.getIsHeadAccountant() != null) {
            factoryUser.setIsHeadAccountant(request.getIsHeadAccountant());
        }

        return factoryUserMapper.toDto(factoryUserRepository.save(factoryUser));
    }

    @Transactional
    public FactoryUserResponseDTO activateFactoryUser(Long id) {
        FactoryUser factoryUser = getFactoryUserOrThrow(id);
        factoryUser.getAccount().setStatus(Account.AccountStatus.ACTIVE);

        accountRepository.save(factoryUser.getAccount());
        return factoryUserMapper.toDto(factoryUser);
    }

    @Transactional
    public FactoryUserResponseDTO deactivateFactoryUser(Long id) {
        FactoryUser factoryUser = getFactoryUserOrThrow(id);
        factoryUser.getAccount().setStatus(Account.AccountStatus.INACTIVE);

        accountRepository.save(factoryUser.getAccount());
        return factoryUserMapper.toDto(factoryUser);
    }

    @Transactional
    public void softDeleteFactoryUser(Long id) {
        FactoryUser factoryUser = getFactoryUserOrThrow(id);
        factoryUser.getAccount().setStatus(Account.AccountStatus.DELETED);

        accountRepository.save(factoryUser.getAccount());
    }

    private FactoryUserResponseDTO createFactoryUser(CreateFactoryUserRequest request,
                                                     FactoryUser.FactoryPosition position,
                                                     Long recyclingCenterId) {
        // Validation et création du compte
        validateCreateRequest(request);
        if (accountRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new ConflictException("Un compte avec cet email existe déjà.");
        }

        Account account = Account.builder()
                .email(request.getEmail().trim().toLowerCase())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .roleType(Account.RoleType.FACTORY_USER)
                .status(Account.AccountStatus.ACTIVE)
                .build();
        Account savedAccount = accountRepository.save(account);

        FactoryUser factoryUser = FactoryUser.builder()
                .account(savedAccount)
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .employeeNumber(request.getEmployeeNumber().trim())
                .position(position)
                .isHeadAccountant(request.getIsHeadAccountant() != null ? request.getIsHeadAccountant() : false)
                .build();

        // AFFECTATION DE L’USINE
        if (recyclingCenterId != null) {
            RecyclingCenter center = recyclingCenterRepository.findById(recyclingCenterId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usine introuvable avec l'ID : " + recyclingCenterId));
            factoryUser.setRecyclingCenter(center);
        }

        FactoryUser savedFactoryUser = factoryUserRepository.save(factoryUser);
        return factoryUserMapper.toDto(savedFactoryUser);
    }

    private void validateCreateRequest(CreateFactoryUserRequest request) {
        if (request == null) {
            throw new BadRequestException("La requête est obligatoire.");
        }

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BadRequestException("L'email est obligatoire.");
        }

        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new BadRequestException("Le mot de passe doit contenir au moins 8 caractères.");
        }

        if (request.getFirstName() == null || request.getFirstName().isBlank()) {
            throw new BadRequestException("Le prénom est obligatoire.");
        }

        if (request.getLastName() == null || request.getLastName().isBlank()) {
            throw new BadRequestException("Le nom est obligatoire.");
        }

        if (request.getEmployeeNumber() == null || request.getEmployeeNumber().isBlank()) {
            throw new BadRequestException("Le numéro employé est obligatoire.");
        }
    }

    private FactoryUser getFactoryUserOrThrow(Long id) {
        return factoryUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employé usine introuvable avec id : " + id));
    }

    @Data
    public static class CreateFactoryUserRequest {
        @Email
        @NotBlank
        private String email;

        private String phone;

        @NotBlank
        private String password;

        @NotBlank
        private String firstName;

        @NotBlank
        private String lastName;

        @NotBlank
        private String employeeNumber;

        private Long recyclingCenterId;

        private Boolean isHeadAccountant = false;
    }

    @Data
    public static class UpdateFactoryUserRequest {
        private String firstName;
        private String lastName;
        private String employeeNumber;
        private FactoryUser.FactoryPosition position;
        private Boolean isHeadAccountant ;
    }

    public FactoryUserResponseDTO assignRecyclingCenter(Long factoryUserId, Long recyclingCenterId) {
        FactoryUser user = getFactoryUserOrThrow(factoryUserId);
        RecyclingCenter center = recyclingCenterRepository.findById(recyclingCenterId)
                .orElseThrow(() -> new ResourceNotFoundException("Usine introuvable"));
        user.setRecyclingCenter(center);
        return factoryUserMapper.toDto(factoryUserRepository.save(user));
    }
}