package com.recyclix.backend.service.workshop;

import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ConflictException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.exception.UnauthorizedException;
import com.recyclix.backend.model.Account;
import com.recyclix.backend.model.FactoryUser;
import com.recyclix.backend.model.Machine;
import com.recyclix.backend.model.MachineIncident;
import com.recyclix.backend.repository.AccountRepository;
import com.recyclix.backend.repository.FactoryUserRepository;
import com.recyclix.backend.repository.MachineIncidentRepository;
import com.recyclix.backend.repository.MachineRepository;
import com.recyclix.backend.util.SecurityUtils;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkshopMaintenanceService {

    private final AccountRepository accountRepository;
    private final FactoryUserRepository factoryUserRepository;
    private final MachineRepository machineRepository;
    private final MachineIncidentRepository machineIncidentRepository;

    @Value("${recyclix.storage.incident-image-path:uploads/workshop/incidents}")
    private String incidentImagePath;

    @Value("${recyclix.storage.allowed-extensions:png,jpg,jpeg,webp}")
    private String allowedExtensions;

    @Value("${recyclix.storage.max-file-size:5242880}")
    private long maxFileSize;

    // =========================================================
    // MACHINES
    // =========================================================

    @Transactional(readOnly = true)
    public List<MachineSummaryResponse> getAllMachines() {
        return machineRepository.findAll().stream()
                .map(this::toMachineSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MachineDetailResponse getMachineById(Long machineId) {
        if (machineId == null) {
            throw new BadRequestException("L'identifiant de la machine est obligatoire.");
        }

        Machine machine = machineRepository.findWithIncidentsById(machineId)
                .orElseThrow(() -> new ResourceNotFoundException("Machine introuvable."));

        return toMachineDetailResponse(machine);
    }

    public MachineDetailResponse updateMachineStatus(Long machineId, UpdateMachineStatusRequest request) {
        if (machineId == null) {
            throw new BadRequestException("L'identifiant de la machine est obligatoire.");
        }

        if (request == null || request.getStatus() == null || request.getStatus().isBlank()) {
            throw new BadRequestException("Le nouveau statut est obligatoire.");
        }

        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new ResourceNotFoundException("Machine introuvable."));

        Machine.MachineStatus newStatus;
        try {
            newStatus = Machine.MachineStatus.valueOf(request.getStatus().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Statut machine invalide.");
        }

        machine.setStatus(newStatus);

        Machine savedMachine = machineRepository.save(machine);
        return toMachineDetailResponse(savedMachine);
    }

    // =========================================================
    // INCIDENTS
    // =========================================================

    @Transactional(readOnly = true)
    public List<IncidentSummaryResponse> getAllIncidents() {
        return machineIncidentRepository.findAll().stream()
                .map(this::toIncidentSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IncidentSummaryResponse> getOpenIncidents() {
        return machineIncidentRepository.findAllByStatus(MachineIncident.IncidentStatus.OPEN).stream()
                .map(this::toIncidentSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public IncidentDetailResponse getIncidentById(Long incidentId) {
        if (incidentId == null) {
            throw new BadRequestException("L'identifiant de l'incident est obligatoire.");
        }

        MachineIncident incident = machineIncidentRepository.findFullById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident introuvable."));

        return toIncidentDetailResponse(incident);
    }

    public IncidentDetailResponse createIncident(CreateIncidentRequest request, MultipartFile incidentImage) {
        if (request == null) {
            throw new BadRequestException("Les données de l'incident sont obligatoires.");
        }

        if (request.getMachineId() == null) {
            throw new BadRequestException("L'identifiant de la machine est obligatoire.");
        }

        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new BadRequestException("La description de l'incident est obligatoire.");
        }

        if (request.getSeverity() == null || request.getSeverity().isBlank()) {
            throw new BadRequestException("La sévérité est obligatoire.");
        }

        FactoryUser currentUser = getAuthenticatedFactoryUser();

        Machine machine = machineRepository.findById(request.getMachineId())
                .orElseThrow(() -> new ResourceNotFoundException("Machine introuvable."));

        MachineIncident.IncidentSeverity severity;
        try {
            severity = MachineIncident.IncidentSeverity.valueOf(request.getSeverity().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Sévérité invalide. Valeurs autorisées: LOW, MEDIUM, HIGH, CRITICAL.");
        }

        MachineIncident.IncidentStatus status = MachineIncident.IncidentStatus.OPEN;
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            try {
                status = MachineIncident.IncidentStatus.valueOf(request.getStatus().trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Statut invalide. Valeurs autorisées: OPEN, IN_PROGRESS, RESOLVED.");
            }
        }

        // Gestion de l'image
        String imageUrl = null;
        if (incidentImage != null && !incidentImage.isEmpty()) {
            imageUrl = storeIncidentImage(incidentImage);
        }

        MachineIncident incident = MachineIncident.builder()
                .machine(machine)
                .reportedBy(currentUser)
                .issueType(request.getDescription().trim())
                .severity(severity)
                .status(status)
                .incidentImageUrl(imageUrl)
                .build();

        MachineIncident savedIncident;
        try {
            savedIncident = machineIncidentRepository.saveAndFlush(incident);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new ConflictException("Impossible de créer l'incident. Vérifiez les contraintes BD.");
        }

        return toIncidentDetailResponse(savedIncident);
    }

    public IncidentDetailResponse resolveIncident(Long incidentId) {
        if (incidentId == null) {
            throw new BadRequestException("L'identifiant de l'incident est obligatoire.");
        }

        MachineIncident incident = machineIncidentRepository.lockById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident introuvable."));

        if (incident.getStatus() == MachineIncident.IncidentStatus.RESOLVED) {
            throw new BadRequestException("Cet incident est déjà résolu.");
        }

        incident.resolve();

        MachineIncident savedIncident = machineIncidentRepository.save(incident);
        return toIncidentDetailResponse(savedIncident);
    }

    // =========================================================
    // SECURITY / HELPERS
    // =========================================================

    private FactoryUser getAuthenticatedFactoryUser() {
        String email = SecurityUtils.getEmail()
                .orElseThrow(() -> new UnauthorizedException("Utilisateur non authentifié."));

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable."));

        if (account.getRoleType() != Account.RoleType.FACTORY_USER) {
            throw new UnauthorizedException("Accès réservé au chef d’atelier.");
        }

        if (account.getFactoryUser() != null) {
            return account.getFactoryUser();
        }

        return factoryUserRepository.findAll().stream()
                .filter(fu -> fu.getAccount() != null && fu.getAccount().getId().equals(account.getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Profil employé usine introuvable."));
    }

    // Stockage de l'image d'incident
    private String storeIncidentImage(MultipartFile file) {
        validateImage(file);

        try {
            String extension = getFileExtension(file.getOriginalFilename());
            Path uploadDir = Path.of(incidentImagePath).toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);

            String fileName = "incident_" + System.currentTimeMillis() + "_" + UUID.randomUUID() + "." + extension;
            Path targetPath = uploadDir.resolve(fileName);

            file.transferTo(targetPath.toFile());

            return incidentImagePath.replace("\\", "/") + "/" + fileName;
        } catch (IOException e) {
            throw new BadRequestException("Impossible de sauvegarder la photo de l'incident : " + e.getMessage());
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
        if (fileName == null || !fileName.contains(".")) {
            throw new BadRequestException("Nom de fichier invalide.");
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    // =========================================================
    // MAPPERS PRIVÉS
    // =========================================================

    private MachineSummaryResponse toMachineSummaryResponse(Machine machine) {
        return MachineSummaryResponse.builder()
                .machineId(machine.getId())
                .name(machine.getName())
                .serialNumber(machine.getSerialNumber())
                .status(machine.getStatus() != null ? machine.getStatus() : null)
                .createdAt(machine.getCreatedAt())
                .incidentsCount(machine.getIncidents() != null ? machine.getIncidents().size() : 0)
                .description(machine.getDescription())
                .photoUrl(machine.getPhotoUrl())
                .build();
    }

    private MachineDetailResponse toMachineDetailResponse(Machine machine) {
        return MachineDetailResponse.builder()
                .machineId(machine.getId())
                .name(machine.getName())
                .serialNumber(machine.getSerialNumber())
                .status(machine.getStatus() != null ? machine.getStatus() : null)
                .createdAt(machine.getCreatedAt())
                .incidentsCount(machine.getIncidents() != null ? machine.getIncidents().size() : 0)
                .description(machine.getDescription())
                .photoUrl(machine.getPhotoUrl())
                .build();
    }

    private IncidentSummaryResponse toIncidentSummaryResponse(MachineIncident incident) {
        return IncidentSummaryResponse.builder()
                .incidentId(incident.getId())
                .machineId(incident.getMachine() != null ? incident.getMachine().getId() : null)
                .machineName(incident.getMachine() != null ? incident.getMachine().getName() : null)
                .severity(incident.getSeverity() != null ? incident.getSeverity().name() : null)
                .status(incident.getStatus() != null ? incident.getStatus().name() : null)
                .reportedAt(incident.getReportedAt())
                .resolvedAt(incident.getResolvedAt())
                .incidentImageUrl(incident.getIncidentImageUrl())   // <-- ajout de la photo dans le résumé
                .build();
    }

    private IncidentDetailResponse toIncidentDetailResponse(MachineIncident incident) {
        return IncidentDetailResponse.builder()
                .incidentId(incident.getId())
                .machineId(incident.getMachine() != null ? incident.getMachine().getId() : null)
                .machineName(incident.getMachine() != null ? incident.getMachine().getName() : null)
                .reportedById(incident.getReportedBy() != null ? incident.getReportedBy().getId() : null)
                .description(incident.getIssueType())
                .severity(incident.getSeverity() != null ? incident.getSeverity().name() : null)
                .status(incident.getStatus() != null ? incident.getStatus().name() : null)
                .reportedAt(incident.getReportedAt())
                .resolvedAt(incident.getResolvedAt())
                .incidentImageUrl(incident.getIncidentImageUrl())   // <-- ajout de la photo dans le détail
                .build();
    }

    public IncidentDetailResponse updateIncidentStatus(Long incidentId, String newStatus) {
        if (incidentId == null) {
            throw new BadRequestException("L'identifiant de l'incident est obligatoire.");
        }
        if (newStatus == null || newStatus.isBlank()) {
            throw new BadRequestException("Le nouveau statut est obligatoire.");
        }

        MachineIncident incident = machineIncidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident introuvable."));

        MachineIncident.IncidentStatus status;
        try {
            status = MachineIncident.IncidentStatus.valueOf(newStatus.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Statut invalide. Valeurs autorisées : OPEN, IN_PROGRESS, RESOLVED");
        }

        incident.setStatus(status);
        if (status == MachineIncident.IncidentStatus.RESOLVED && incident.getResolvedAt() == null) {
            incident.setResolvedAt(LocalDateTime.now());
        }
        MachineIncident savedIncident = machineIncidentRepository.save(incident);
        return toIncidentDetailResponse(savedIncident);
    }

    // =========================================================
    // REQUEST / RESPONSE CLASSES
    // =========================================================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateMachineStatusRequest {
        @NotBlank(message = "Le statut est obligatoire.")
        private String status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateIncidentRequest {
        @NotNull(message = "L'identifiant de la machine est obligatoire.")
        private Long machineId;

        @NotBlank(message = "La description est obligatoire.")
        private String description;

        @NotBlank(message = "La sévérité est obligatoire.")
        private String severity;

        private String status;  // optionnel, par défaut OPEN
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MachineSummaryResponse {
        private Long machineId;
        private String name;
        private String serialNumber;
        private Machine.MachineStatus status;
        private LocalDateTime createdAt;
        private Integer incidentsCount;
        private String description;
        private String photoUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MachineDetailResponse {
        private Long machineId;
        private String name;
        private String serialNumber;
        private Machine.MachineStatus status;
        private LocalDateTime createdAt;
        private Integer incidentsCount;
        private String description;
        private String photoUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IncidentSummaryResponse {
        private Long incidentId;
        private Long machineId;
        private String machineName;
        private String severity;
        private String status;
        private LocalDateTime reportedAt;
        private LocalDateTime resolvedAt;
        private String incidentImageUrl;   // Nouveau champ
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IncidentDetailResponse {
        private Long incidentId;
        private Long machineId;
        private String machineName;
        private Long reportedById;
        private String description;
        private String severity;
        private String status;
        private LocalDateTime reportedAt;
        private LocalDateTime resolvedAt;
        private String incidentImageUrl;   // Nouveau champ
    }


}