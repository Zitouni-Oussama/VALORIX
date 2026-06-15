package com.recyclix.backend.service.workshop;

import com.recyclix.backend.dto.account.AccountResponseDTO;
import com.recyclix.backend.dto.account.AccountUpdateDTO;
import com.recyclix.backend.dto.factory_user.FactoryUserResponseDTO;
import com.recyclix.backend.dto.factory_user.FactoryUserUpdateDTO;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.exception.UnauthorizedException;
import com.recyclix.backend.mapper.AccountMapper;
import com.recyclix.backend.mapper.FactoryUserMapper;
import com.recyclix.backend.model.Account;
import com.recyclix.backend.model.FactoryUser;
import com.recyclix.backend.repository.AccountRepository;
import com.recyclix.backend.repository.FactoryUserRepository;
import com.recyclix.backend.util.SecurityUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkshopProfileService {

    private final AccountRepository accountRepository;
    private final FactoryUserRepository factoryUserRepository;
    private final AccountMapper accountMapper;
    private final FactoryUserMapper factoryUserMapper;

    @Value("${recyclix.storage.factory-user-profile-path:uploads/factory-user/profile}")
    private String factoryUserProfilePath;

    @Value("${recyclix.storage.allowed-extensions}")
    private String allowedExtensions;

    @Value("${recyclix.storage.max-file-size}")
    private long maxFileSize;

    @Transactional(readOnly = true)
    public WorkshopProfileResponse getMyProfile() {
        Account account = getAuthenticatedAccount();
        FactoryUser factoryUser = getFactoryUserFromAccount(account);

        return WorkshopProfileResponse.builder()
                .account(accountMapper.toDto(account))
                .factoryUser(factoryUserMapper.toDto(factoryUser))
                .build();
    }

    public WorkshopProfileResponse updateMyProfile(
            AccountUpdateDTO accountDto,
            FactoryUserUpdateDTO factoryUserDto,
            MultipartFile profileImage
    ) {
        Account account = getAuthenticatedAccount();
        FactoryUser factoryUser = getFactoryUserFromAccount(account);

        validateUpdateInputs(accountDto, factoryUserDto, profileImage);
        sanitizeForbiddenFields(accountDto, factoryUserDto);

        String oldImageUrl = account.getProfileImageUrl();
        String newImageUrl = null;

        if (accountDto != null) {
            accountMapper.updateEntityFromDto(accountDto, account);
        }

        if (factoryUserDto != null) {
            factoryUserMapper.updateEntityFromDto(factoryUserDto, factoryUser);
        }

        if (profileImage != null && !profileImage.isEmpty()) {
            newImageUrl = storeProfileImage(profileImage, account.getId());
            account.setProfileImageUrl(newImageUrl);
        }

        account = accountRepository.save(account);
        factoryUser = factoryUserRepository.save(factoryUser);

        if (newImageUrl != null && StringUtils.hasText(oldImageUrl) && !oldImageUrl.equals(newImageUrl)) {
            deletePhysicalFileIfExists(oldImageUrl);
        }

        return WorkshopProfileResponse.builder()
                .account(accountMapper.toDto(account))
                .factoryUser(factoryUserMapper.toDto(factoryUser))
                .build();
    }

    public WorkshopProfileResponse removeMyProfileImage() {
        Account account = getAuthenticatedAccount();
        FactoryUser factoryUser = getFactoryUserFromAccount(account);

        String oldImageUrl = account.getProfileImageUrl();
        account.setProfileImageUrl(null);
        account = accountRepository.save(account);

        deletePhysicalFileIfExists(oldImageUrl);

        return WorkshopProfileResponse.builder()
                .account(accountMapper.toDto(account))
                .factoryUser(factoryUserMapper.toDto(factoryUser))
                .build();
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private Account getAuthenticatedAccount() {
        String email = SecurityUtils.getEmail()
                .orElseThrow(() -> new UnauthorizedException("Utilisateur non authentifié."));

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable."));

        if (account.getRoleType() != Account.RoleType.FACTORY_USER) {
            throw new UnauthorizedException("Accès réservé au chef d’atelier.");
        }

        return account;
    }

    private FactoryUser getFactoryUserFromAccount(Account account) {
        if (account.getFactoryUser() != null) {
            return account.getFactoryUser();
        }

        return factoryUserRepository.findAll().stream()
                .filter(fu -> fu.getAccount() != null && fu.getAccount().getId().equals(account.getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Profil employé usine introuvable."));
    }

    private void validateUpdateInputs(
            AccountUpdateDTO accountDto,
            FactoryUserUpdateDTO factoryUserDto,
            MultipartFile profileImage
    ) {
        boolean noAccountUpdate = accountDto == null;
        boolean noFactoryUserUpdate = factoryUserDto == null;
        boolean noImageUpdate = profileImage == null || profileImage.isEmpty();

        if (noAccountUpdate && noFactoryUserUpdate && noImageUpdate) {
            throw new BadRequestException("Aucune donnée à mettre à jour.");
        }

        if (profileImage != null && !profileImage.isEmpty()) {
            validateFile(profileImage);
        }
    }

    private void sanitizeForbiddenFields(AccountUpdateDTO accountDto, FactoryUserUpdateDTO factoryUserDto) {
        if (accountDto != null) {
            accountDto.setStatus(null);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.getSize() > maxFileSize) {
            throw new BadRequestException("Le fichier dépasse la taille maximale autorisée.");
        }

        String filename = file.getOriginalFilename();
        String extension = getExtension(filename);

        List<String> allowed = parseAllowedExtensions();
        if (!allowed.contains(extension.toLowerCase())) {
            throw new BadRequestException("Extension de fichier non autorisée.");
        }
    }

    private String storeProfileImage(MultipartFile file, Long accountId) {
        try {
            String extension = getExtension(file.getOriginalFilename());
            String fileName = "factory-user-profile-" + accountId + "-" + UUID.randomUUID() + "." + extension;

            Path dir = Path.of(factoryUserProfilePath).toAbsolutePath().normalize();
            Files.createDirectories(dir);

            Path target = dir.resolve(fileName);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }

            return "/uploads/factory-user/profile/" + fileName;
        } catch (IOException e) {
            throw new BadRequestException("Impossible d'enregistrer l'image de profil.");
        }
    }

    private void deletePhysicalFileIfExists(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            return;
        }

        try {
            String fileName = Path.of(fileUrl).getFileName().toString();
            Path filePath = Path.of(factoryUserProfilePath).toAbsolutePath().normalize().resolve(fileName);

            Files.deleteIfExists(filePath);
        } catch (Exception ignored) {
        }
    }

    private String getExtension(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            throw new BadRequestException("Nom de fichier invalide.");
        }

        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private List<String> parseAllowedExtensions() {
        if (!StringUtils.hasText(allowedExtensions)) {
            return List.of("png", "jpg", "jpeg", "webp");
        }

        return List.of(allowedExtensions.split(","))
                .stream()
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(StringUtils::hasText)
                .toList();
    }

    // =========================================================
    // RESPONSE CLASSES
    // =========================================================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WorkshopProfileResponse {
        private AccountResponseDTO account;
        private FactoryUserResponseDTO factoryUser;
    }
}