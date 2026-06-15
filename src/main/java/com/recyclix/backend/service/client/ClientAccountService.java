package com.recyclix.backend.service.client;

import com.recyclix.backend.dto.account.AccountResponseDTO;
import com.recyclix.backend.dto.account.AccountUpdateDTO;
import com.recyclix.backend.dto.client.ClientResponseDTO;
import com.recyclix.backend.dto.client.ClientUpdateDTO;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.exception.UnauthorizedException;
import com.recyclix.backend.mapper.AccountMapper;
import com.recyclix.backend.mapper.ClientMapper;
import com.recyclix.backend.model.Account;
import com.recyclix.backend.model.Client;
import com.recyclix.backend.repository.AccountRepository;
import com.recyclix.backend.repository.ClientRepository;
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
public class ClientAccountService {

    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;
    private final AccountMapper accountMapper;
    private final ClientMapper clientMapper;

    @Value("${recyclix.storage.client-profile-path:uploads/client/profile}")
    private String clientProfilePath;

    @Value("${recyclix.storage.allowed-extensions}")
    private String allowedExtensions;

    @Value("${recyclix.storage.max-file-size}")
    private long maxFileSize;

    @Transactional(readOnly = true)
    public ClientAccountProfileResponse getMyProfile() {
        Account account = getAuthenticatedAccount();
        Client client = getClientFromAccount(account);

        return ClientAccountProfileResponse.builder()
                .account(accountMapper.toDto(account))
                .client(clientMapper.toDto(client))
                .build();
    }

    public ClientAccountProfileResponse updateMyProfile(
            AccountUpdateDTO accountDto,
            ClientUpdateDTO clientDto,
            MultipartFile profileImage
    ) {
        Account account = getAuthenticatedAccount();
        Client client = getClientFromAccount(account);

        validateUpdateInputs(accountDto, clientDto, profileImage);
        sanitizeForbiddenFields(accountDto, clientDto);

        String oldImageUrl = account.getProfileImageUrl();
        String newImageUrl = null;

        if (accountDto != null) {
            accountMapper.updateEntityFromDto(accountDto, account);
        }

        if (clientDto != null) {
            clientMapper.updateEntityFromDto(clientDto, client);
        }

        if (profileImage != null && !profileImage.isEmpty()) {
            newImageUrl = storeProfileImage(profileImage, account.getId());
            account.setProfileImageUrl(newImageUrl);
        }

//        if (profileImage != null && !profileImage.isEmpty()) {
//            account.setProfileImageUrl("test-image.jpg");
//        }

        account = accountRepository.save(account);
        client = clientRepository.save(client);

        if (newImageUrl != null && StringUtils.hasText(oldImageUrl) && !oldImageUrl.equals(newImageUrl)) {
            deletePhysicalFileIfExists(oldImageUrl);
        }

        return ClientAccountProfileResponse.builder()
                .account(accountMapper.toDto(account))
                .client(clientMapper.toDto(client))
                .build();
    }

    public ClientAccountProfileResponse removeMyProfileImage() {
        Account account = getAuthenticatedAccount();
        Client client = getClientFromAccount(account);

        String oldImageUrl = account.getProfileImageUrl();
        account.setProfileImageUrl(null);
        account = accountRepository.save(account);

        deletePhysicalFileIfExists(oldImageUrl);

        return ClientAccountProfileResponse.builder()
                .account(accountMapper.toDto(account))
                .client(clientMapper.toDto(client))
                .build();
    }

    @Transactional(readOnly = true)
    public ClientResponseDTO getClientByAccountId(Long accountId) {
        if (accountId == null) {
            throw new BadRequestException("L'identifiant du compte est obligatoire.");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable."));

        Client client = getClientFromAccount(account);
        return clientMapper.toDto(client);
    }

    private Account getAuthenticatedAccount() {
        String email = SecurityUtils.getEmail()
                .orElseThrow(() -> new UnauthorizedException("Utilisateur non authentifié."));

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable."));

        if (account.getRoleType() != Account.RoleType.CLIENT) {
            throw new UnauthorizedException("Accès réservé au client.");
        }

        return account;
    }

    private Client getClientFromAccount(Account account) {
        if (account.getClient() == null) {
            throw new ResourceNotFoundException("Profil client introuvable pour ce compte.");
        }
        return account.getClient();
    }

    private void validateUpdateInputs(
            AccountUpdateDTO accountDto,
            ClientUpdateDTO clientDto,
            MultipartFile profileImage
    ) {
        boolean noAccountUpdate = (accountDto == null);
        boolean noClientUpdate = (clientDto == null);
        boolean noImageUpdate = (profileImage == null || profileImage.isEmpty());

        if (noAccountUpdate && noClientUpdate && noImageUpdate) {
            throw new BadRequestException("Aucune donnée de mise à jour fournie.");
        }
    }

    private void sanitizeForbiddenFields(AccountUpdateDTO accountDto, ClientUpdateDTO clientDto) {
        if (accountDto != null) {
            if (StringUtils.hasText(accountDto.getPasswordHash())) {
                throw new BadRequestException(
                        "Le mot de passe ne se modifie pas ici. Utilisez le service de mot de passe."
                );
            }

            if (accountDto.getStatus() != null) {
                throw new BadRequestException("Le statut du compte ne peut pas être modifié par le client.");
            }

        }

        if (clientDto != null && clientDto.getTotalPoints() != null) {
            throw new BadRequestException("Le total des points ne peut pas être modifié par le client.");
        }
    }

    private String storeProfileImage(MultipartFile file, Long accountId) {
        validateImage(file);

        try {
            String extension = getFileExtension(file.getOriginalFilename());

            Path uploadDir = Path.of(clientProfilePath).toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);

            String fileName = "client_" + accountId + "_" + UUID.randomUUID() + "." + extension;
            Path targetPath = uploadDir.resolve(fileName);

            file.transferTo(targetPath.toFile());

            return clientProfilePath.replace("\\", "/") + "/" + fileName;
        } catch (IOException e) {
            e.printStackTrace();
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
    public static class ClientAccountProfileResponse {
        private AccountResponseDTO account;
        private ClientResponseDTO client;
    }
}