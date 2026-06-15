package com.recyclix.backend.service.collector;

import com.recyclix.backend.dto.truck.TruckRequestDTO;
import com.recyclix.backend.dto.truck.TruckResponseDTO;
import com.recyclix.backend.dto.truck.TruckUpdateDTO;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ConflictException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.exception.UnauthorizedException;
import com.recyclix.backend.mapper.TruckMapper;
import com.recyclix.backend.model.Account;
import com.recyclix.backend.model.Collector;
import com.recyclix.backend.model.Truck;
import com.recyclix.backend.repository.AccountRepository;
import com.recyclix.backend.repository.CollectorRepository;
import com.recyclix.backend.repository.TruckRepository;
import com.recyclix.backend.util.SecurityUtils;
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
public class CollectorTruckService {

    private final AccountRepository accountRepository;
    private final CollectorRepository collectorRepository;
    private final TruckRepository truckRepository;
    private final TruckMapper truckMapper;

    @Value("${recyclix.storage.collector-truck-path:uploads/collector/truck}")
    private String collectorTruckPath;

    @Value("${recyclix.storage.collector-grey-card-path:uploads/collector/truck/grey-card}")
    private String collectorGreyCardPath;

    @Value("${recyclix.storage.collector-license-path:uploads/collector/truck/license}")
    private String collectorLicensePath;

    @Value("${recyclix.storage.allowed-extensions}")
    private String allowedExtensions;

    @Value("${recyclix.storage.max-file-size}")
    private long maxFileSize;

    @Transactional(readOnly = true)
    public TruckResponseDTO getMyTruck() {
        Truck truck = getMyTruckEntity();
        return truckMapper.toResponseDTO(truck);
    }

    public TruckResponseDTO createMyTruck(
            TruckRequestDTO dto,
            MultipartFile truckPhoto,
            MultipartFile greyCardImage,
            MultipartFile drivingLicenseImage
    ) {
        if (dto == null) {
            throw new BadRequestException("Les informations du camion sont obligatoires.");
        }

        Collector collector = getAuthenticatedCollector();

        if (collector.getTruck() != null) {
            throw new ConflictException("Un camion existe déjà pour ce collecteur.");
        }

        sanitizeForbiddenFieldsOnCreate(dto);
        validateRequiredCreateFields(dto);

        // Conversion BigDecimal -> Double pour la capacité
        Double capacityKgValue = (dto.getCapacityKg() != null) ? dto.getCapacityKg().doubleValue() : null;

        Truck truck = Truck.builder()
                .collector(collector)
                .plateNumber(dto.getPlateNumber())
                .brand(dto.getBrand())               // marque depuis le DTO
                .model(dto.getModel())
                .capacityKg(capacityKgValue)         // capacité correctement initialisée
                .isActive(resolveActiveStatus(dto.getStatus()))
                .build();

        if (truckPhoto != null && !truckPhoto.isEmpty()) {
            truck.setTruckPhotoUrl(storeTruckPhoto(truckPhoto, collector.getId()));
        }

        if (greyCardImage != null && !greyCardImage.isEmpty()) {
            truck.setGreyCardImageUrl(storeGreyCard(greyCardImage, collector.getId()));
        }

        if (drivingLicenseImage != null && !drivingLicenseImage.isEmpty()) {
            truck.setDrivingLicenseImageUrl(storeDrivingLicense(drivingLicenseImage, collector.getId()));
        }

        Truck saved = truckRepository.save(truck);
        return truckMapper.toResponseDTO(saved);
    }

    public TruckResponseDTO updateMyTruck(
            TruckUpdateDTO dto,
            MultipartFile truckPhoto,
            MultipartFile greyCardImage,
            MultipartFile drivingLicenseImage
    ) {
        Truck truck = getMyTruckEntity();

        if (dto == null && isAllFilesEmpty(truckPhoto, greyCardImage, drivingLicenseImage)) {
            throw new BadRequestException("Aucune donnée de mise à jour fournie.");
        }

        if (dto != null) {
            sanitizeForbiddenFieldsOnUpdate(dto);

            if (StringUtils.hasText(dto.getPlateNumber())) {
                truck.setPlateNumber(dto.getPlateNumber().trim());
            }

            if (dto.getBrand() != null) {
                truck.setBrand(dto.getBrand());
            }

            if (dto.getModel() != null) {
                truck.setModel(dto.getModel());
            }

            // Mise à jour de la capacité si fournie
            if (dto.getCapacityKg() != null) {
                truck.setCapacityKg(dto.getCapacityKg().doubleValue());
            }

            if (StringUtils.hasText(dto.getStatus())) {
                truck.setIsActive(resolveActiveStatus(dto.getStatus()));
            }
        }

        if (truckPhoto != null && !truckPhoto.isEmpty()) {
            String oldValue = truck.getTruckPhotoUrl();
            String newValue = storeTruckPhoto(truckPhoto, truck.getCollector().getId());
            truck.setTruckPhotoUrl(newValue);
            deletePhysicalFileIfExists(oldValue);
        }

        if (greyCardImage != null && !greyCardImage.isEmpty()) {
            String oldValue = truck.getGreyCardImageUrl();
            String newValue = storeGreyCard(greyCardImage, truck.getCollector().getId());
            truck.setGreyCardImageUrl(newValue);
            deletePhysicalFileIfExists(oldValue);
        }

        if (drivingLicenseImage != null && !drivingLicenseImage.isEmpty()) {
            String oldValue = truck.getDrivingLicenseImageUrl();
            String newValue = storeDrivingLicense(drivingLicenseImage, truck.getCollector().getId());
            truck.setDrivingLicenseImageUrl(newValue);
            deletePhysicalFileIfExists(oldValue);
        }

        Truck saved = truckRepository.save(truck);
        return truckMapper.toResponseDTO(saved);
    }

    public TruckResponseDTO activateMyTruck() {
        Truck truck = getMyTruckEntity();
        truck.activate();
        return truckMapper.toResponseDTO(truckRepository.save(truck));
    }

    public TruckResponseDTO deactivateMyTruck() {
        Truck truck = getMyTruckEntity();
        truck.deactivate();
        return truckMapper.toResponseDTO(truckRepository.save(truck));
    }

    public TruckResponseDTO removeMyTruckPhoto() {
        Truck truck = getMyTruckEntity();
        String oldValue = truck.getTruckPhotoUrl();
        truck.setTruckPhotoUrl(null);
        Truck saved = truckRepository.save(truck);
        deletePhysicalFileIfExists(oldValue);
        return truckMapper.toResponseDTO(saved);
    }

    public TruckResponseDTO removeMyGreyCardImage() {
        Truck truck = getMyTruckEntity();
        String oldValue = truck.getGreyCardImageUrl();
        truck.setGreyCardImageUrl(null);
        Truck saved = truckRepository.save(truck);
        deletePhysicalFileIfExists(oldValue);
        return truckMapper.toResponseDTO(saved);
    }

    public TruckResponseDTO removeMyDrivingLicenseImage() {
        Truck truck = getMyTruckEntity();
        String oldValue = truck.getDrivingLicenseImageUrl();
        truck.setDrivingLicenseImageUrl(null);
        Truck saved = truckRepository.save(truck);
        deletePhysicalFileIfExists(oldValue);
        return truckMapper.toResponseDTO(saved);
    }

    private Truck getMyTruckEntity() {
        Collector collector = getAuthenticatedCollector();
        Truck truck = collector.getTruck();
        if (truck == null) {
            throw new ResourceNotFoundException("Aucun camion trouvé pour ce collecteur.");
        }
        return truck;
    }

    private Account getAuthenticatedCollectorAccount() {
        String email = SecurityUtils.getEmail()
                .orElseThrow(() -> new UnauthorizedException("Utilisateur non authentifié."));
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable."));
        if (account.getRoleType() != Account.RoleType.COLLECTOR) {
            throw new UnauthorizedException("Accès réservé au collecteur.");
        }
        return account;
    }

    private Collector getAuthenticatedCollector() {
        Account account = getAuthenticatedCollectorAccount();
        if (account.getCollector() == null) {
            throw new ResourceNotFoundException("Profil collecteur introuvable.");
        }
        return account.getCollector();
    }

    private void validateRequiredCreateFields(TruckRequestDTO dto) {
        if (!StringUtils.hasText(dto.getPlateNumber())) {
            throw new BadRequestException("Le numéro de plaque est obligatoire.");
        }
        if (dto.getCapacityKg() == null) {
            throw new BadRequestException("La capacité (kg) est obligatoire.");
        }
        if (dto.getCapacityKg().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("La capacité doit être supérieure à zéro.");
        }
        if (!StringUtils.hasText(dto.getStatus())) {
            throw new BadRequestException("Le statut du camion est obligatoire.");
        }
    }

    private void sanitizeForbiddenFieldsOnCreate(TruckRequestDTO dto) {
        if (dto.getCollectorId() != null) {
            throw new BadRequestException("Le collecteur ne doit pas être envoyé dans la création du camion.");
        }
        if (StringUtils.hasText(dto.getTruckPhotoUrl())
                || StringUtils.hasText(dto.getGreyCardImageUrl())
                || StringUtils.hasText(dto.getDrivingLicenseImageUrl())) {
            throw new BadRequestException("Les images/documents doivent être envoyés via fichiers, pas via JSON.");
        }
    }

    private void sanitizeForbiddenFieldsOnUpdate(TruckUpdateDTO dto) {
        if (dto.getCollectorId() != null) {
            throw new BadRequestException("Le collecteur lié au camion ne peut pas être modifié.");
        }
        if (StringUtils.hasText(dto.getTruckPhotoUrl())
                || StringUtils.hasText(dto.getGreyCardImageUrl())
                || StringUtils.hasText(dto.getDrivingLicenseImageUrl())) {
            throw new BadRequestException("Les images/documents doivent être envoyés via fichiers, pas via JSON.");
        }
    }

    private boolean isAllFilesEmpty(
            MultipartFile truckPhoto,
            MultipartFile greyCardImage,
            MultipartFile drivingLicenseImage
    ) {
        return (truckPhoto == null || truckPhoto.isEmpty())
                && (greyCardImage == null || greyCardImage.isEmpty())
                && (drivingLicenseImage == null || drivingLicenseImage.isEmpty());
    }

    private Boolean resolveActiveStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return true;
        }
        String normalized = status.trim().toUpperCase();
        return switch (normalized) {
            case "ACTIVE", "ACTIF", "EN_SERVICE", "AVAILABLE" -> true;
            case "INACTIVE", "INACTIF", "OUT_OF_SERVICE", "DISABLED" -> false;
            default -> throw new BadRequestException("Statut camion invalide.");
        };
    }

    private String storeTruckPhoto(MultipartFile file, Long collectorId) {
        validateImage(file);
        return storeFile(file, collectorTruckPath, "truck_" + collectorId);
    }

    private String storeGreyCard(MultipartFile file, Long collectorId) {
        validateImage(file);
        return storeFile(file, collectorGreyCardPath, "grey_card_" + collectorId);
    }

    private String storeDrivingLicense(MultipartFile file, Long collectorId) {
        validateImage(file);
        return storeFile(file, collectorLicensePath, "license_" + collectorId);
    }

    private String storeFile(MultipartFile file, String rootPath, String prefix) {
        try {
            String extension = getFileExtension(file.getOriginalFilename());
            Path uploadDir = Path.of(rootPath).toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);

            String fileName = prefix + "_" + UUID.randomUUID() + "." + extension;
            Path targetPath = uploadDir.resolve(fileName);

            file.transferTo(targetPath.toFile());

            return rootPath.replace("\\", "/") + "/" + fileName;
        } catch (IOException e) {
            throw new BadRequestException("Impossible de sauvegarder le fichier : " + e.getMessage());
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
            throw new BadRequestException("Le fichier dépasse la taille maximale autorisée.");
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
}