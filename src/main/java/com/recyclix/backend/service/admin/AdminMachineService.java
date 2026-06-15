//package com.recyclix.backend.service.admin;
//
//import com.recyclix.backend.dto.machine.MachineRequestDTO;
//import com.recyclix.backend.dto.machine.MachineResponseDTO;
//import com.recyclix.backend.dto.machine.MachineSummaryDTO;
//import com.recyclix.backend.dto.machine.MachineUpdateDTO;
//import com.recyclix.backend.dto.machine_incident.MachineIncidentResponseDTO;
//import com.recyclix.backend.dto.machine_incident.MachineIncidentSummaryDTO;
//import com.recyclix.backend.exception.BadRequestException;
//import com.recyclix.backend.exception.ConflictException;
//import com.recyclix.backend.exception.ResourceNotFoundException;
//import com.recyclix.backend.mapper.MachineIncidentMapper;
//import com.recyclix.backend.mapper.MachineMapper;
//import com.recyclix.backend.model.Machine;
//import com.recyclix.backend.model.MachineIncident;
//import com.recyclix.backend.repository.MachineIncidentRepository;
//import com.recyclix.backend.repository.MachineRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import org.springframework.web.multipart.MultipartFile;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.io.IOException;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class AdminMachineService {
//
//    // Ajoutez un champ pour le chemin de stockage
//    @Value("${recyclix.storage.machines-image-path:uploads/workshop/machines}")
//    private String machinesImagePath;
//
//    private final MachineRepository machineRepository;
//    private final MachineIncidentRepository machineIncidentRepository;
//    private final MachineMapper machineMapper;
//    private final MachineIncidentMapper incidentMapper;
//
//    // ==================== MACHINES ====================
//
//    @Transactional(readOnly = true)
//    public Page<MachineSummaryDTO> getAllMachines(int page, int size) {
//        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
//        return machineRepository.findAll(pageable).map(machineMapper::toSummaryDto);
//    }
//
//    @Transactional(readOnly = true)
//    public MachineResponseDTO getMachineById(Long id) {
//        Machine machine = getMachineOrThrow(id);
//        return machineMapper.toDto(machine);
//    }
//
//    // Dans AdminMachineService.java
//
//    public MachineResponseDTO createMachine(MachineRequestDTO request, MultipartFile photo) {
//        validateMachineRequest(request);
//        if (machineRepository.existsBySerialNumber(request.getSerialNumber().trim())) {
//            throw new ConflictException("Une machine avec ce numéro de série existe déjà.");
//        }
//        Machine machine = machineMapper.toEntity(request);
//        machine.setSerialNumber(request.getSerialNumber().trim());
//        machine.setName(request.getName().trim());
//        machine.setDescription(request.getDescription());
//        machine.setStatus(request.getStatus() != null ? request.getStatus() : Machine.MachineStatus.WORKING);
//
//        if (photo != null && !photo.isEmpty()) {
//            String photoUrl = storeMachineImage(photo);
//            machine.setPhotoUrl(photoUrl);
//        }
//
//        Machine saved = machineRepository.save(machine);
//        return machineMapper.toDto(saved);
//    }
//
//    public MachineResponseDTO updateMachine(Long id, MachineUpdateDTO request, MultipartFile photo) {
//        Machine machine = getMachineOrThrow(id);
//        if (request.getName() != null && !request.getName().isBlank()) {
//            machine.setName(request.getName().trim());
//        }
//        if (request.getSerialNumber() != null && !request.getSerialNumber().isBlank()) {
//            String newSerial = request.getSerialNumber().trim();
//            machineRepository.findBySerialNumber(newSerial)
//                    .filter(existing -> !existing.getId().equals(id))
//                    .ifPresent(existing -> {
//                        throw new ConflictException("Ce numéro de série est déjà utilisé.");
//                    });
//            machine.setSerialNumber(newSerial);
//        }
//        if (request.getStatus() != null) {
//            machine.setStatus(request.getStatus());
//        }
//        if (request.getDescription() != null) {
//            machine.setDescription(request.getDescription());
//        }
//        if (photo != null && !photo.isEmpty()) {
//            // Optionnel : supprimer l'ancienne photo physique
//            String photoUrl = storeMachineImage(photo);
//            machine.setPhotoUrl(photoUrl);
//        }
//        Machine updated = machineRepository.save(machine);
//        return machineMapper.toDto(updated);
//    }
//
//
//    public void deleteMachine(Long id) {
//        Machine machine = getMachineOrThrow(id);
//        machineRepository.delete(machine);
//    }
//
//    // ==================== INCIDENTS ====================
//
//    @Transactional(readOnly = true)
//    public Page<MachineIncidentSummaryDTO> getAllIncidents(int page, int size, String severity, String status) {
//        Pageable pageable = PageRequest.of(page, size, Sort.by("reportedAt").descending());
//        Page<MachineIncident> incidents;
//
//        if (severity != null && status != null) {
//            incidents = machineIncidentRepository.findAll(
//                    (root, query, cb) -> cb.and(
//                            cb.equal(root.get("severity"), MachineIncident.IncidentSeverity.valueOf(severity)),
//                            cb.equal(root.get("status"), MachineIncident.IncidentStatus.valueOf(status))
//                    ), pageable);
//        } else if (severity != null) {
//            incidents = machineIncidentRepository.findAllBySeverity(
//                    MachineIncident.IncidentSeverity.valueOf(severity), pageable);
//        } else if (status != null) {
//            incidents = machineIncidentRepository.findAllByStatus(
//                    MachineIncident.IncidentStatus.valueOf(status), pageable);
//        } else {
//            incidents = machineIncidentRepository.findAll(pageable);
//        }
//        return incidents.map(incidentMapper::toSummaryDto);
//    }
//
//    @Transactional(readOnly = true)
//    public MachineIncidentResponseDTO getIncidentById(Long id) {
//        MachineIncident incident = machineIncidentRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Incident introuvable avec l'ID : " + id));
//        return incidentMapper.toDto(incident);
//    }
//
//    public MachineIncidentResponseDTO updateIncidentStatus(Long incidentId, String newStatus) {
//        MachineIncident incident = machineIncidentRepository.findById(incidentId)
//                .orElseThrow(() -> new ResourceNotFoundException("Incident introuvable avec l'ID : " + incidentId));
//
//        MachineIncident.IncidentStatus statusEnum;
//        try {
//            statusEnum = MachineIncident.IncidentStatus.valueOf(newStatus.toUpperCase());
//        } catch (IllegalArgumentException e) {
//            throw new BadRequestException("Statut invalide. Valeurs autorisées: OPEN, IN_PROGRESS, RESOLVED");
//        }
//
//        incident.setStatus(statusEnum);
//        if (statusEnum == MachineIncident.IncidentStatus.RESOLVED && incident.getResolvedAt() == null) {
//            incident.setResolvedAt(java.time.LocalDateTime.now());
//        }
//        MachineIncident saved = machineIncidentRepository.save(incident);
//        return incidentMapper.toDto(saved);
//    }
//
//    public void deleteIncident(Long incidentId) {
//        MachineIncident incident = machineIncidentRepository.findById(incidentId)
//                .orElseThrow(() -> new ResourceNotFoundException("Incident introuvable avec l'ID : " + incidentId));
//        machineIncidentRepository.delete(incident);
//    }
//
//    // ==================== HELPERS ====================
//
//    private Machine getMachineOrThrow(Long id) {
//        return machineRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Machine introuvable avec l'ID : " + id));
//    }
//
//    private void validateMachineRequest(MachineRequestDTO request) {
//        if (request.getName() == null || request.getName().isBlank()) {
//            throw new BadRequestException("Le nom de la machine est obligatoire.");
//        }
//        if (request.getSerialNumber() == null || request.getSerialNumber().isBlank()) {
//            throw new BadRequestException("Le numéro de série est obligatoire.");
//        }
//    }
//
//    // Méthode pour sauvegarder l'image
//    private String storeMachineImage(MultipartFile file) {
//        if (file == null || file.isEmpty()) return null;
//        try {
//            String extension = getFileExtension(file.getOriginalFilename());
//            String fileName = "machine_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + "." + extension;
//            Path uploadDir = Paths.get(machinesImagePath).toAbsolutePath().normalize();
//            Files.createDirectories(uploadDir);
//            Path targetPath = uploadDir.resolve(fileName);
//            file.transferTo(targetPath.toFile());
//            return "/" + machinesImagePath.replace("\\", "/") + "/" + fileName;
//        } catch (IOException e) {
//            throw new BadRequestException("Impossible de sauvegarder la photo : " + e.getMessage());
//        }
//    }
//
//    private String getFileExtension(String fileName) {
//        int dotIndex = fileName.lastIndexOf('.');
//        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1).toLowerCase();
//    }
//}




package com.recyclix.backend.service.admin;

import com.recyclix.backend.dto.machine.MachineRequestDTO;
import com.recyclix.backend.dto.machine.MachineResponseDTO;
import com.recyclix.backend.dto.machine.MachineSummaryDTO;
import com.recyclix.backend.dto.machine.MachineUpdateDTO;
import com.recyclix.backend.dto.machine_incident.MachineIncidentResponseDTO;
import com.recyclix.backend.dto.machine_incident.MachineIncidentSummaryDTO;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ConflictException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.mapper.MachineIncidentMapper;
import com.recyclix.backend.mapper.MachineMapper;
import com.recyclix.backend.model.Machine;
import com.recyclix.backend.model.MachineIncident;
import com.recyclix.backend.repository.MachineIncidentRepository;
import com.recyclix.backend.repository.MachineRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminMachineService {

    @Value("${recyclix.storage.machines-image-path:uploads/workshop/machines}")
    private String machinesImagePath;

    private final MachineRepository machineRepository;
    private final MachineIncidentRepository machineIncidentRepository;
    private final MachineMapper machineMapper;
    private final MachineIncidentMapper incidentMapper;

    // ==================== MACHINES ====================

    @Transactional(readOnly = true)
    public Page<MachineSummaryDTO> getAllMachines(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return machineRepository.findAll(pageable).map(machineMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public MachineResponseDTO getMachineById(Long id) {
        Machine machine = getMachineOrThrow(id);
        return machineMapper.toDto(machine);
    }

    // Création avec upload photo
    public MachineResponseDTO createMachine(MachineRequestDTO request, MultipartFile photo) {
        validateMachineRequest(request);
        if (machineRepository.existsBySerialNumber(request.getSerialNumber().trim())) {
            throw new ConflictException("Une machine avec ce numéro de série existe déjà.");
        }
        Machine machine = machineMapper.toEntity(request);
        machine.setSerialNumber(request.getSerialNumber().trim());
        machine.setName(request.getName().trim());
        machine.setDescription(request.getDescription());
        machine.setStatus(request.getStatus() != null ? request.getStatus() : Machine.MachineStatus.WORKING);

        if (photo != null && !photo.isEmpty()) {
            String photoUrl = storeMachineImage(photo);
            machine.setPhotoUrl(photoUrl);
        }

        Machine saved = machineRepository.save(machine);
        return machineMapper.toDto(saved);
    }

    // Mise à jour avec upload photo (supprime l'ancienne si nouvelle fournie)
    public MachineResponseDTO updateMachine(Long id, MachineUpdateDTO request, MultipartFile photo) {
        Machine machine = getMachineOrThrow(id);

        if (request.getName() != null && !request.getName().isBlank()) {
            machine.setName(request.getName().trim());
        }
        if (request.getSerialNumber() != null && !request.getSerialNumber().isBlank()) {
            String newSerial = request.getSerialNumber().trim();
            machineRepository.findBySerialNumber(newSerial)
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> {
                        throw new ConflictException("Ce numéro de série est déjà utilisé.");
                    });
            machine.setSerialNumber(newSerial);
        }
        if (request.getStatus() != null) {
            machine.setStatus(request.getStatus());
        }
        if (request.getDescription() != null) {
            machine.setDescription(request.getDescription());
        }

        // Gestion de la photo : supprimer l'ancienne si nouvelle fournie
        if (photo != null && !photo.isEmpty()) {
            // Supprimer l'ancienne photo si elle existe
            if (machine.getPhotoUrl() != null && !machine.getPhotoUrl().isBlank()) {
                deleteOldImage(machine.getPhotoUrl());
            }
            String newPhotoUrl = storeMachineImage(photo);
            machine.setPhotoUrl(newPhotoUrl);
        }

        Machine updated = machineRepository.save(machine);
        return machineMapper.toDto(updated);
    }

    // Mise à jour sans photo (pour compatibilité, si appelé sans fichier)
    public MachineResponseDTO updateMachine(Long id, MachineUpdateDTO request) {
        return updateMachine(id, request, null);
    }

    public void deleteMachine(Long id) {
        Machine machine = getMachineOrThrow(id);
        // Supprimer la photo associée si elle existe
        if (machine.getPhotoUrl() != null && !machine.getPhotoUrl().isBlank()) {
            deleteOldImage(machine.getPhotoUrl());
        }
        machineRepository.delete(machine);
    }

    // ==================== INCIDENTS ====================

    @Transactional(readOnly = true)
    public Page<MachineIncidentSummaryDTO> getAllIncidents(int page, int size, String severity, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("reportedAt").descending());
        Page<MachineIncident> incidents;

        if (severity != null && status != null) {
            incidents = machineIncidentRepository.findAll(
                    (root, query, cb) -> cb.and(
                            cb.equal(root.get("severity"), MachineIncident.IncidentSeverity.valueOf(severity)),
                            cb.equal(root.get("status"), MachineIncident.IncidentStatus.valueOf(status))
                    ), pageable);
        } else if (severity != null) {
            incidents = machineIncidentRepository.findAllBySeverity(
                    MachineIncident.IncidentSeverity.valueOf(severity), pageable);
        } else if (status != null) {
            incidents = machineIncidentRepository.findAllByStatus(
                    MachineIncident.IncidentStatus.valueOf(status), pageable);
        } else {
            incidents = machineIncidentRepository.findAll(pageable);
        }
        return incidents.map(incidentMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public MachineIncidentResponseDTO getIncidentById(Long id) {
        MachineIncident incident = machineIncidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident introuvable avec l'ID : " + id));
        return incidentMapper.toDto(incident);
    }

    public MachineIncidentResponseDTO updateIncidentStatus(Long incidentId, String newStatus) {
        MachineIncident incident = machineIncidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident introuvable avec l'ID : " + incidentId));

        MachineIncident.IncidentStatus statusEnum;
        try {
            statusEnum = MachineIncident.IncidentStatus.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Statut invalide. Valeurs autorisées: OPEN, IN_PROGRESS, RESOLVED");
        }

        incident.setStatus(statusEnum);
        if (statusEnum == MachineIncident.IncidentStatus.RESOLVED && incident.getResolvedAt() == null) {
            incident.setResolvedAt(java.time.LocalDateTime.now());
        }
        MachineIncident saved = machineIncidentRepository.save(incident);
        return incidentMapper.toDto(saved);
    }

    public void deleteIncident(Long incidentId) {
        MachineIncident incident = machineIncidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident introuvable avec l'ID : " + incidentId));
        machineIncidentRepository.delete(incident);
    }

    // ==================== HELPERS ====================

    private Machine getMachineOrThrow(Long id) {
        return machineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Machine introuvable avec l'ID : " + id));
    }

    private void validateMachineRequest(MachineRequestDTO request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("Le nom de la machine est obligatoire.");
        }
        if (request.getSerialNumber() == null || request.getSerialNumber().isBlank()) {
            throw new BadRequestException("Le numéro de série est obligatoire.");
        }
    }

    // Sauvegarde d'une nouvelle photo
    private String storeMachineImage(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        try {
            String extension = getFileExtension(file.getOriginalFilename());
            String fileName = "machine_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + "." + extension;
            Path uploadDir = Paths.get(machinesImagePath).toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);
            Path targetPath = uploadDir.resolve(fileName);
            file.transferTo(targetPath.toFile());
            return "/" + machinesImagePath.replace("\\", "/") + "/" + fileName;
        } catch (IOException e) {
            throw new BadRequestException("Impossible de sauvegarder la photo : " + e.getMessage());
        }
    }

    // Suppression d'une ancienne photo
    private void deleteOldImage(String oldPhotoUrl) {
        if (oldPhotoUrl == null || oldPhotoUrl.isBlank()) return;
        try {
            // Enlever le premier slash si présent pour obtenir le chemin relatif
            String relativePath = oldPhotoUrl.startsWith("/") ? oldPhotoUrl.substring(1) : oldPhotoUrl;
            Path filePath = Paths.get(relativePath).toAbsolutePath().normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log seulement, ne pas bloquer l'opération
            System.err.println("Impossible de supprimer l'ancienne photo: " + e.getMessage());
        }
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1).toLowerCase();
    }

    @Transactional(readOnly = true)
    public Page<MachineSummaryDTO> getFilteredMachines(String name, String serialNumber, String status, int page, int size) {
        Specification<Machine> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            // ✅ NOUVEAU : recherche par numéro de série
            if (serialNumber != null && !serialNumber.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("serialNumber")), "%" + serialNumber.toLowerCase() + "%"));
            }
            if (status != null && !status.isBlank()) {
                try {
                    Machine.MachineStatus statusEnum = Machine.MachineStatus.valueOf(status.toUpperCase());
                    predicates.add(cb.equal(root.get("status"), statusEnum));
                } catch (IllegalArgumentException ignored) {}
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return machineRepository.findAll(spec, pageable).map(machineMapper::toSummaryDto);
    }
}