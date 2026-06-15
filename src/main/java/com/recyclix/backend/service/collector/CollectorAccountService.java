package com.recyclix.backend.service.collector;

import com.recyclix.backend.dto.account.AccountResponseDTO;
import com.recyclix.backend.dto.account.AccountUpdateDTO;
import com.recyclix.backend.dto.collector.CollectorResponseDTO;
import com.recyclix.backend.dto.collector.CollectorUpdateDTO;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.exception.UnauthorizedException;
import com.recyclix.backend.mapper.AccountMapper;
import com.recyclix.backend.mapper.CollectorMapper;
import com.recyclix.backend.model.Account;
import com.recyclix.backend.model.Collector;
import com.recyclix.backend.repository.AccountRepository;
import com.recyclix.backend.repository.CollectorRepository;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CollectorAccountService {

    private final AccountRepository accountRepository;
    private final CollectorRepository collectorRepository;
    private final AccountMapper accountMapper;
    private final CollectorMapper collectorMapper;

    @Value("${recyclix.storage.collector-profile-path:uploads/collector/profile}")
    private String collectorProfilePath;

    @Value("${recyclix.storage.allowed-extensions}")
    private String allowedExtensions;

    @Value("${recyclix.storage.max-file-size}")
    private long maxFileSize;

    @Transactional(readOnly = true)
    public CollectorAccountProfileResponse getMyProfile() {
        Account account = getAuthenticatedAccount();
        Collector collector = getCollectorFromAccount(account);

        return CollectorAccountProfileResponse.builder()
                .account(accountMapper.toDto(account))
                .collector(collectorMapper.toDto(collector))
                .build();
    }

    public CollectorAccountProfileResponse updateMyProfile(
            AccountUpdateDTO accountDto,
            CollectorUpdateDTO collectorDto,
            MultipartFile profileImage
    ) {
        Account account = getAuthenticatedAccount();
        Collector collector = getCollectorFromAccount(account);

        validateUpdateInputs(accountDto, collectorDto, profileImage);
        sanitizeForbiddenFields(accountDto, collectorDto);

        String oldImageUrl = account.getProfileImageUrl();
        String newImageUrl = null;

        if (accountDto != null) {
            accountMapper.updateEntityFromDto(accountDto, account);
        }

        if (collectorDto != null) {
            collectorMapper.updateEntityFromDto(collectorDto, collector);
        }

        if (profileImage != null && !profileImage.isEmpty()) {
            newImageUrl = storeProfileImage(profileImage, account.getId());
            account.setProfileImageUrl(newImageUrl);
        }

        account = accountRepository.save(account);
        collector = collectorRepository.save(collector);

        if (newImageUrl != null && StringUtils.hasText(oldImageUrl) && !oldImageUrl.equals(newImageUrl)) {
            deletePhysicalFileIfExists(oldImageUrl);
        }

        return CollectorAccountProfileResponse.builder()
                .account(accountMapper.toDto(account))
                .collector(collectorMapper.toDto(collector))
                .build();
    }

    public CollectorAccountProfileResponse removeMyProfileImage() {
        Account account = getAuthenticatedAccount();
        Collector collector = getCollectorFromAccount(account);

        String oldImageUrl = account.getProfileImageUrl();
        account.setProfileImageUrl(null);
        account = accountRepository.save(account);

        deletePhysicalFileIfExists(oldImageUrl);

        return CollectorAccountProfileResponse.builder()
                .account(accountMapper.toDto(account))
                .collector(collectorMapper.toDto(collector))
                .build();
    }

    @Transactional(readOnly = true)
    public CollectorResponseDTO getCollectorByAccountId(Long accountId) {
        if (accountId == null) {
            throw new BadRequestException("L'identifiant du compte est obligatoire.");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable."));

        Collector collector = getCollectorFromAccount(account);
        return collectorMapper.toDto(collector);
    }

    private Account getAuthenticatedAccount() {
        String email = SecurityUtils.getEmail()
                .orElseThrow(() -> new UnauthorizedException("Utilisateur non authentifié."));

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable."));

        if (account.getRoleType() != Account.RoleType.COLLECTOR) {
            throw new UnauthorizedException("Accès réservé au collecteur.");
        }

        return account;
    }

    private Collector getCollectorFromAccount(Account account) {
        if (account.getCollector() == null) {
            throw new ResourceNotFoundException("Profil collecteur introuvable pour ce compte.");
        }
        return account.getCollector();
    }

    private void validateUpdateInputs(
            AccountUpdateDTO accountDto,
            CollectorUpdateDTO collectorDto,
            MultipartFile profileImage
    ) {
        boolean noAccountUpdate = (accountDto == null);
        boolean noCollectorUpdate = (collectorDto == null);
        boolean noImageUpdate = (profileImage == null || profileImage.isEmpty());

        if (noAccountUpdate && noCollectorUpdate && noImageUpdate) {
            throw new BadRequestException("Aucune donnée de mise à jour fournie.");
        }
    }

    private void sanitizeForbiddenFields(AccountUpdateDTO accountDto, CollectorUpdateDTO collectorDto) {
        if (accountDto != null) {
            if (StringUtils.hasText(accountDto.getPasswordHash())) {
                throw new BadRequestException(
                        "Le mot de passe ne se modifie pas ici. Utilisez le service de mot de passe."
                );
            }

            if (accountDto.getStatus() != null) {
                throw new BadRequestException("Le statut du compte ne peut pas être modifié par le collecteur.");
            }
        }

        if (collectorDto != null) {
            if (collectorDto.getIsVerified() != null) {
                throw new BadRequestException("Le statut de vérification ne peut pas être modifié par le collecteur.");
            }

            if (collectorDto.getAverageRating() != null) {
                throw new BadRequestException("La note moyenne ne peut pas être modifiée par le collecteur.");
            }

            if (collectorDto.getCurrentLatitude() != null || collectorDto.getCurrentLongitude() != null) {
                throw new BadRequestException(
                        "La position actuelle se met à jour via le service de collecte/localisation."
                );
            }
        }
    }

    private String storeProfileImage(MultipartFile file, Long accountId) {
        validateImage(file);

        try {
            String extension = getFileExtension(file.getOriginalFilename());

            Path uploadDir = Path.of(collectorProfilePath).toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);

            String fileName = "collector_" + accountId + "_" + UUID.randomUUID() + "." + extension;
            Path targetPath = uploadDir.resolve(fileName);

            file.transferTo(targetPath.toFile());

            return collectorProfilePath.replace("\\", "/") + "/" + fileName;
        } catch (IOException e) {
            throw new BadRequestException("Impossible de sauvegarder l'image de profil : " + e.getMessage());
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Le fichier image est obligatoire.");
        }

        String extension = getFileExtension(file.getOriginalFilename());

        boolean allowed = List.of(allowedExtensions.toLowerCase().split(","))
                .contains(extension.toLowerCase());

        if (!allowed) {
            throw new BadRequestException("Format d'image non supporté.");
        }

        if (file.getSize() > maxFileSize) {
            throw new BadRequestException("L'image dépasse la taille maximale autorisée.");
        }
    }

    private String getFileExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            throw new BadRequestException("Nom de fichier invalide.");
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex < 0 || lastDotIndex == fileName.length() - 1) {
            throw new BadRequestException("Extension de fichier introuvable.");
        }

        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    private void deletePhysicalFileIfExists(String storedImageUrl) {
        if (!StringUtils.hasText(storedImageUrl)) {
            return;
        }

        try {
            Path filePath = Path.of(storedImageUrl).toAbsolutePath().normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CollectorAccountProfileResponse {
        private AccountResponseDTO account;
        private CollectorResponseDTO collector;
    }
}