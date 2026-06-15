//package com.recyclix.backend.service.hr;
//
//import com.recyclix.backend.dto.worker_absence.WorkerAbsenceRequestDTO;
//import com.recyclix.backend.dto.worker_absence.WorkerAbsenceResponseDTO;
//import com.recyclix.backend.dto.worker_absence.WorkerAbsenceSummaryDTO;
//import com.recyclix.backend.dto.worker_absence.WorkerAbsenceUpdateDTO;
//import com.recyclix.backend.exception.BadRequestException;
//import com.recyclix.backend.exception.ResourceNotFoundException;
//import com.recyclix.backend.exception.UnauthorizedException;
//import com.recyclix.backend.mapper.WorkerAbsenceMapper;
//import com.recyclix.backend.model.Employee;
//import com.recyclix.backend.model.FactoryUser;
//import com.recyclix.backend.model.RecyclingCenter;
//import com.recyclix.backend.model.WorkerAbsence;
//import com.recyclix.backend.repository.EmployeeRepository;
//import com.recyclix.backend.repository.FactoryUserRepository;
//import com.recyclix.backend.repository.WorkerAbsenceRepository;
//import com.recyclix.backend.util.SecurityUtils;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class WorkerAbsenceService {
//
//    private final WorkerAbsenceRepository absenceRepository;
//    private final FactoryUserRepository factoryUserRepository;
//    private final EmployeeRepository employeeRepository;
//    private final WorkerAbsenceMapper absenceMapper;
//
//    public WorkerAbsenceResponseDTO reportAbsence(WorkerAbsenceRequestDTO dto) {
//        FactoryUser recordedBy = getAuthenticatedFactoryUser();
//
//        Employee employee = employeeRepository.findById(dto.getEmployeeId())
//                .orElseThrow(() -> new ResourceNotFoundException("Employé introuvable."));
//
//        if (!Boolean.TRUE.equals(employee.getActive())) {
//            throw new BadRequestException("Impossible de déclarer une absence pour un employé inactif.");
//        }
//
//        WorkerAbsence absence = absenceMapper.toEntity(dto);
//        absence.setEmployee(employee);
//        absence.setRecordedBy(recordedBy);
//
//        validateDates(absence.getStartDate(), absence.getEndDate());
//
//        WorkerAbsence savedAbsence = absenceRepository.save(absence);
//        return absenceMapper.toResponseDTO(savedAbsence);
//    }
//
//    @Transactional(readOnly = true)
//    public List<WorkerAbsenceSummaryDTO> getAllAbsences() {
//        RecyclingCenter center = getCurrentUserRecyclingCenter();
//        Long centerId = center.getId();
//
//        List<WorkerAbsence> absences = absenceRepository.findAll().stream()
//                .filter(a -> a.getEmployee() != null
//                        && a.getEmployee().getRecyclingCenter() != null
//                        && a.getEmployee().getRecyclingCenter().getId().equals(centerId))
//                .collect(Collectors.toList());
//
//        return absences.stream()
//                .map(absenceMapper::toSummaryDTO)
//                .collect(Collectors.toList());
//    }
//
//    @Transactional(readOnly = true)
//    public List<WorkerAbsenceSummaryDTO> getAbsencesForToday() {
//        LocalDate today = LocalDate.now();
//
//        return absenceRepository.findAll()
//                .stream()
//                .filter(a -> !a.getStartDate().isAfter(today))
//                .filter(a -> a.getEndDate() == null || !a.getEndDate().isBefore(today))
//                .map(absenceMapper::toSummaryDTO)
//                .toList();
//    }
//
//    @Transactional(readOnly = true)
//    public List<WorkerAbsenceSummaryDTO> getAbsencesByEmployee(Long employeeId) {
//        return absenceRepository.findByEmployeeId(employeeId)
//                .stream()
//                .map(absenceMapper::toSummaryDTO)
//                .toList();
//    }
//
//    public WorkerAbsenceResponseDTO updateAbsence(Long id, WorkerAbsenceUpdateDTO dto) {
//        WorkerAbsence absence = absenceRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Absence introuvable avec l'ID : " + id));
//
//        if (dto.getEmployeeId() != null) {
//            Employee employee = employeeRepository.findById(dto.getEmployeeId())
//                    .orElseThrow(() -> new ResourceNotFoundException("Employé introuvable."));
//
//            if (!Boolean.TRUE.equals(employee.getActive())) {
//                throw new BadRequestException("Impossible d'affecter une absence à un employé inactif.");
//            }
//
//            absence.setEmployee(employee);
//        }
//
//        absenceMapper.updateEntityFromDTO(dto, absence);
//
//        validateDates(absence.getStartDate(), absence.getEndDate());
//
//        WorkerAbsence updatedAbsence = absenceRepository.save(absence);
//        return absenceMapper.toResponseDTO(updatedAbsence);
//    }
//
//    public void deleteAbsence(Long id) {
//        WorkerAbsence absence = absenceRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Absence introuvable avec l'ID : " + id));
//
//        absenceRepository.delete(absence);
//    }
//
//    private FactoryUser getAuthenticatedFactoryUser() {
//        Long accountId = SecurityUtils.getAccountId()
//                .orElseThrow(() -> new UnauthorizedException("Utilisateur non authentifié."));
//
//        return factoryUserRepository.findByAccountId(accountId)
//                .orElseThrow(() -> new ResourceNotFoundException("Employé usine introuvable pour ce compte."));
//    }
//
//    private void validateDates(LocalDate startDate, LocalDate endDate) {
//        if (startDate == null) {
//            throw new BadRequestException("La date de début est obligatoire.");
//        }
//
//        if (endDate != null && endDate.isBefore(startDate)) {
//            throw new BadRequestException("La date de fin ne peut pas être avant la date de début.");
//        }
//    }
//
//    private RecyclingCenter getCurrentUserRecyclingCenter() {
//        Long accountId = SecurityUtils.getAccountId()
//                .orElseThrow(() -> new UnauthorizedException("Utilisateur non authentifié"));
//        FactoryUser factoryUser = factoryUserRepository.findByAccountId(accountId)
//                .orElseThrow(() -> new ResourceNotFoundException("Employé usine introuvable"));
//        if (factoryUser.getRecyclingCenter() == null) {
//            throw new BadRequestException("Aucune usine associée à ce comptable.");
//        }
//        return factoryUser.getRecyclingCenter();
//    }
//}
















































package com.recyclix.backend.service.hr;

import com.recyclix.backend.dto.worker_absence.WorkerAbsenceRequestDTO;
import com.recyclix.backend.dto.worker_absence.WorkerAbsenceResponseDTO;
import com.recyclix.backend.dto.worker_absence.WorkerAbsenceSummaryDTO;
import com.recyclix.backend.dto.worker_absence.WorkerAbsenceUpdateDTO;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.exception.UnauthorizedException;
import com.recyclix.backend.mapper.WorkerAbsenceMapper;
import com.recyclix.backend.model.Employee;
import com.recyclix.backend.model.FactoryUser;
import com.recyclix.backend.model.RecyclingCenter;
import com.recyclix.backend.model.WorkerAbsence;
import com.recyclix.backend.repository.EmployeeRepository;
import com.recyclix.backend.repository.FactoryUserRepository;
import com.recyclix.backend.repository.WorkerAbsenceRepository;
import com.recyclix.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkerAbsenceService {

    private final WorkerAbsenceRepository absenceRepository;
    private final FactoryUserRepository factoryUserRepository;
    private final EmployeeRepository employeeRepository;
    private final WorkerAbsenceMapper absenceMapper;

    // Helper pour récupérer l'usine du comptable connecté
    private RecyclingCenter getCurrentUserRecyclingCenter() {
        Long accountId = SecurityUtils.getAccountId()
                .orElseThrow(() -> new UnauthorizedException("Utilisateur non authentifié."));
        FactoryUser factoryUser = factoryUserRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Employé usine introuvable pour ce compte."));
        if (factoryUser.getRecyclingCenter() == null) {
            throw new BadRequestException("Aucune usine associée à ce comptable.");
        }
        return factoryUser.getRecyclingCenter();
    }

    public WorkerAbsenceResponseDTO reportAbsence(WorkerAbsenceRequestDTO dto) {
        FactoryUser recordedBy = getAuthenticatedFactoryUser(); // méthode existante
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employé introuvable."));

        // Vérification : l'employé doit appartenir à l'usine du comptable
        RecyclingCenter center = getCurrentUserRecyclingCenter();
        if (employee.getRecyclingCenter() == null || !employee.getRecyclingCenter().getId().equals(center.getId())) {
            throw new BadRequestException("Cet employé n'appartient pas à votre usine.");
        }

        if (!Boolean.TRUE.equals(employee.getActive())) {
            throw new BadRequestException("Impossible de déclarer une absence pour un employé inactif.");
        }

        WorkerAbsence absence = absenceMapper.toEntity(dto);
        absence.setEmployee(employee);
        absence.setRecordedBy(recordedBy);

        validateDates(absence.getStartDate(), absence.getEndDate());

        WorkerAbsence savedAbsence = absenceRepository.save(absence);
        return absenceMapper.toResponseDTO(savedAbsence);
    }

    @Transactional(readOnly = true)
    public List<WorkerAbsenceSummaryDTO> getAllAbsences() {
        RecyclingCenter center = getCurrentUserRecyclingCenter();
        Long centerId = center.getId();

        return absenceRepository.findAll().stream()
                .filter(a -> a.getEmployee() != null
                        && a.getEmployee().getRecyclingCenter() != null
                        && a.getEmployee().getRecyclingCenter().getId().equals(centerId))
                .map(absenceMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkerAbsenceSummaryDTO> getAbsencesForToday() {
        LocalDate today = LocalDate.now();
        RecyclingCenter center = getCurrentUserRecyclingCenter();
        Long centerId = center.getId();

        return absenceRepository.findAll().stream()
                .filter(a -> a.getEmployee() != null
                        && a.getEmployee().getRecyclingCenter() != null
                        && a.getEmployee().getRecyclingCenter().getId().equals(centerId))
                .filter(a -> !a.getStartDate().isAfter(today))
                .filter(a -> a.getEndDate() == null || !a.getEndDate().isBefore(today))
                .map(absenceMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkerAbsenceSummaryDTO> getAbsencesByEmployee(Long employeeId) {
        RecyclingCenter center = getCurrentUserRecyclingCenter();
        Long centerId = center.getId();
        return absenceRepository.findByEmployeeId(employeeId).stream()
                .filter(a -> a.getEmployee().getRecyclingCenter() != null
                        && a.getEmployee().getRecyclingCenter().getId().equals(centerId))
                .map(absenceMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }

    public WorkerAbsenceResponseDTO updateAbsence(Long id, WorkerAbsenceUpdateDTO dto) {
        WorkerAbsence absence = absenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Absence introuvable avec l'ID : " + id));

        // Vérification que l'absence concerne un employé de l'usine du comptable
        RecyclingCenter center = getCurrentUserRecyclingCenter();
        if (absence.getEmployee() == null || absence.getEmployee().getRecyclingCenter() == null ||
                !absence.getEmployee().getRecyclingCenter().getId().equals(center.getId())) {
            throw new BadRequestException("Cette absence n'appartient pas à votre usine.");
        }

        if (dto.getEmployeeId() != null) {
            Employee employee = employeeRepository.findById(dto.getEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employé introuvable."));
            if (employee.getRecyclingCenter() == null || !employee.getRecyclingCenter().getId().equals(center.getId())) {
                throw new BadRequestException("Le nouvel employé n'appartient pas à votre usine.");
            }
            if (!Boolean.TRUE.equals(employee.getActive())) {
                throw new BadRequestException("Impossible d'affecter une absence à un employé inactif.");
            }
            absence.setEmployee(employee);
        }

        absenceMapper.updateEntityFromDTO(dto, absence);
        validateDates(absence.getStartDate(), absence.getEndDate());

        WorkerAbsence updatedAbsence = absenceRepository.save(absence);
        return absenceMapper.toResponseDTO(updatedAbsence);
    }

    public void deleteAbsence(Long id) {
        WorkerAbsence absence = absenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Absence introuvable avec l'ID : " + id));
        // Vérification de l'usine avant suppression
        RecyclingCenter center = getCurrentUserRecyclingCenter();
        if (absence.getEmployee() == null || absence.getEmployee().getRecyclingCenter() == null ||
                !absence.getEmployee().getRecyclingCenter().getId().equals(center.getId())) {
            throw new BadRequestException("Cette absence n'appartient pas à votre usine.");
        }
        absenceRepository.delete(absence);
    }

    private FactoryUser getAuthenticatedFactoryUser() {
        Long accountId = SecurityUtils.getAccountId()
                .orElseThrow(() -> new UnauthorizedException("Utilisateur non authentifié."));
        return factoryUserRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Employé usine introuvable pour ce compte."));
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            throw new BadRequestException("La date de début est obligatoire.");
        }
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new BadRequestException("La date de fin ne peut pas être avant la date de début.");
        }
    }
}