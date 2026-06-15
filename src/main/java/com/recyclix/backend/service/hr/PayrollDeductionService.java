//package com.recyclix.backend.service.hr;
//
//import com.recyclix.backend.dto.payroll.DeductionRequestDTO;
//import com.recyclix.backend.dto.payroll.DeductionResponseDTO;
//import com.recyclix.backend.dto.payroll.DeductionSummaryDTO;
//import com.recyclix.backend.dto.payroll.DeductionUpdateDTO;
//import com.recyclix.backend.exception.BadRequestException;
//import com.recyclix.backend.exception.ResourceNotFoundException;
//import com.recyclix.backend.exception.UnauthorizedException;
//import com.recyclix.backend.mapper.PayrollDeductionMapper;
//import com.recyclix.backend.model.Employee;
//import com.recyclix.backend.model.FactoryUser;
//import com.recyclix.backend.model.PayrollDeduction;
//import com.recyclix.backend.model.RecyclingCenter;
//import com.recyclix.backend.repository.EmployeeRepository;
//import com.recyclix.backend.repository.FactoryUserRepository;
//import com.recyclix.backend.repository.PayrollDeductionRepository;
//import com.recyclix.backend.util.SecurityUtils;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDate;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class PayrollDeductionService {
//
//    private final PayrollDeductionRepository deductionRepository;
//    private final FactoryUserRepository factoryUserRepository;
//    private final EmployeeRepository employeeRepository;
//    private final PayrollDeductionMapper deductionMapper;
//
//    public DeductionResponseDTO addDeduction(DeductionRequestDTO request) {
//        FactoryUser recordedBy = getAuthenticatedFactoryUser();
//
//        Employee employee = getActiveEmployee(request.getEmployeeId());
//
//        PayrollDeduction deduction = deductionMapper.toEntity(request);
//        deduction.setEmployee(employee);
//        deduction.setRecordedBy(recordedBy);
//
//        validateDeduction(deduction);
//
//        PayrollDeduction savedDeduction = deductionRepository.save(deduction);
//        return deductionMapper.toResponseDTO(savedDeduction);
//    }
//
//    @Transactional(readOnly = true)
//    public List<DeductionSummaryDTO> getAllDeductions() {
//        return deductionRepository.findAll()
//                .stream()
//                .map(deductionMapper::toSummaryDTO)
//                .toList();
//    }
//
//    @Transactional(readOnly = true)
//    public DeductionResponseDTO getDeductionById(Long id) {
//        PayrollDeduction deduction = getDeduction(id);
//        return deductionMapper.toResponseDTO(deduction);
//    }
//
//    @Transactional(readOnly = true)
//    public List<DeductionSummaryDTO> getDeductionsByEmployee(Long employeeId) {
//        return deductionRepository.findByEmployeeId(employeeId)
//                .stream()
//                .map(deductionMapper::toSummaryDTO)
//                .toList();
//    }
//
//    @Transactional(readOnly = true)
//    public List<DeductionSummaryDTO> getMonthlyDeductions(int year, int month) {
//        if (month < 1 || month > 12) {
//            throw new BadRequestException("Le mois doit être entre 1 et 12.");
//        }
//
//        if (year < 2000) {
//            throw new BadRequestException("L'année est invalide.");
//        }
//
//        LocalDate start = LocalDate.of(year, month, 1);
//        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
//
//        return deductionRepository.findByDeductionDateBetween(start, end)
//                .stream()
//                .map(deductionMapper::toSummaryDTO)
//                .toList();
//    }
//
//    public DeductionResponseDTO updateDeduction(Long id, DeductionUpdateDTO request) {
//        PayrollDeduction deduction = getDeduction(id);
//
//        if (request.getEmployeeId() != null) {
//            Employee employee = getActiveEmployee(request.getEmployeeId());
//            deduction.setEmployee(employee);
//        }
//
//        deductionMapper.updateEntityFromDTO(request, deduction);
//
//        validateDeduction(deduction);
//
//        PayrollDeduction updatedDeduction = deductionRepository.save(deduction);
//        return deductionMapper.toResponseDTO(updatedDeduction);
//    }
//
//    public void deleteDeduction(Long id) {
//        PayrollDeduction deduction = getDeduction(id);
//        deductionRepository.delete(deduction);
//    }
//
//    private PayrollDeduction getDeduction(Long id) {
//        return deductionRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Déduction introuvable avec l'ID : " + id));
//    }
//
//    private Employee getActiveEmployee(Long employeeId) {
//        Employee employee = employeeRepository.findById(employeeId)
//                .orElseThrow(() -> new ResourceNotFoundException("Employé introuvable."));
//
//        if (!Boolean.TRUE.equals(employee.getActive())) {
//            throw new BadRequestException("Impossible d'appliquer une déduction à un employé inactif.");
//        }
//
//        return employee;
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
//    private void validateDeduction(PayrollDeduction deduction) {
//        if (deduction.getEmployee() == null) {
//            throw new BadRequestException("L'employé est obligatoire.");
//        }
//
//        if (deduction.getAmount() == null || deduction.getAmount().signum() <= 0) {
//            throw new BadRequestException("Le montant doit être supérieur à zéro.");
//        }
//
//        if (deduction.getDeductionDate() == null) {
//            throw new BadRequestException("La date de déduction est obligatoire.");
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

import com.recyclix.backend.dto.payroll.DeductionRequestDTO;
import com.recyclix.backend.dto.payroll.DeductionResponseDTO;
import com.recyclix.backend.dto.payroll.DeductionSummaryDTO;
import com.recyclix.backend.dto.payroll.DeductionUpdateDTO;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.exception.UnauthorizedException;
import com.recyclix.backend.mapper.PayrollDeductionMapper;
import com.recyclix.backend.model.Employee;
import com.recyclix.backend.model.FactoryUser;
import com.recyclix.backend.model.PayrollDeduction;
import com.recyclix.backend.model.RecyclingCenter;
import com.recyclix.backend.repository.EmployeeRepository;
import com.recyclix.backend.repository.FactoryUserRepository;
import com.recyclix.backend.repository.PayrollDeductionRepository;
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
public class PayrollDeductionService {

    private final PayrollDeductionRepository deductionRepository;
    private final FactoryUserRepository factoryUserRepository;
    private final EmployeeRepository employeeRepository;
    private final PayrollDeductionMapper deductionMapper;

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

    public DeductionResponseDTO addDeduction(DeductionRequestDTO request) {
        FactoryUser recordedBy = getAuthenticatedFactoryUser();

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employé introuvable."));

        // Vérifier que l'employé appartient à l'usine du comptable
        RecyclingCenter center = getCurrentUserRecyclingCenter();
        if (employee.getRecyclingCenter() == null || !employee.getRecyclingCenter().getId().equals(center.getId())) {
            throw new BadRequestException("Cet employé n'appartient pas à votre usine.");
        }

        if (!Boolean.TRUE.equals(employee.getActive())) {
            throw new BadRequestException("Impossible d'appliquer une déduction à un employé inactif.");
        }

        PayrollDeduction deduction = deductionMapper.toEntity(request);
        deduction.setEmployee(employee);
        deduction.setRecordedBy(recordedBy);

        validateDeduction(deduction);

        PayrollDeduction savedDeduction = deductionRepository.save(deduction);
        return deductionMapper.toResponseDTO(savedDeduction);
    }

    @Transactional(readOnly = true)
    public List<DeductionSummaryDTO> getAllDeductions() {
        RecyclingCenter center = getCurrentUserRecyclingCenter();
        Long centerId = center.getId();

        return deductionRepository.findAll().stream()
                .filter(d -> d.getEmployee() != null
                        && d.getEmployee().getRecyclingCenter() != null
                        && d.getEmployee().getRecyclingCenter().getId().equals(centerId))
                .map(deductionMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DeductionResponseDTO getDeductionById(Long id) {
        PayrollDeduction deduction = getDeduction(id);
        // Vérification supplémentaire que la déduction concerne l'usine du comptable
        RecyclingCenter center = getCurrentUserRecyclingCenter();
        if (deduction.getEmployee() == null || deduction.getEmployee().getRecyclingCenter() == null ||
                !deduction.getEmployee().getRecyclingCenter().getId().equals(center.getId())) {
            throw new BadRequestException("Cette déduction n'appartient pas à votre usine.");
        }
        return deductionMapper.toResponseDTO(deduction);
    }

    @Transactional(readOnly = true)
    public List<DeductionSummaryDTO> getDeductionsByEmployee(Long employeeId) {
        RecyclingCenter center = getCurrentUserRecyclingCenter();
        Long centerId = center.getId();
        return deductionRepository.findByEmployeeId(employeeId).stream()
                .filter(d -> d.getEmployee().getRecyclingCenter() != null
                        && d.getEmployee().getRecyclingCenter().getId().equals(centerId))
                .map(deductionMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DeductionSummaryDTO> getMonthlyDeductions(int year, int month) {
        if (month < 1 || month > 12) {
            throw new BadRequestException("Le mois doit être entre 1 et 12.");
        }
        if (year < 2000) {
            throw new BadRequestException("L'année est invalide.");
        }
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        RecyclingCenter center = getCurrentUserRecyclingCenter();
        Long centerId = center.getId();

        return deductionRepository.findByDeductionDateBetween(start, end).stream()
                .filter(d -> d.getEmployee() != null
                        && d.getEmployee().getRecyclingCenter() != null
                        && d.getEmployee().getRecyclingCenter().getId().equals(centerId))
                .map(deductionMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }

    public DeductionResponseDTO updateDeduction(Long id, DeductionUpdateDTO request) {
        PayrollDeduction deduction = getDeduction(id);

        // Vérifier que la déduction concerne l'usine du comptable
        RecyclingCenter center = getCurrentUserRecyclingCenter();
        if (deduction.getEmployee() == null || deduction.getEmployee().getRecyclingCenter() == null ||
                !deduction.getEmployee().getRecyclingCenter().getId().equals(center.getId())) {
            throw new BadRequestException("Cette déduction n'appartient pas à votre usine.");
        }

        if (request.getEmployeeId() != null) {
            Employee employee = employeeRepository.findById(request.getEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employé introuvable."));
            if (employee.getRecyclingCenter() == null || !employee.getRecyclingCenter().getId().equals(center.getId())) {
                throw new BadRequestException("Le nouvel employé n'appartient pas à votre usine.");
            }
            if (!Boolean.TRUE.equals(employee.getActive())) {
                throw new BadRequestException("Impossible d'appliquer une déduction à un employé inactif.");
            }
            deduction.setEmployee(employee);
        }

        deductionMapper.updateEntityFromDTO(request, deduction);
        validateDeduction(deduction);

        PayrollDeduction updatedDeduction = deductionRepository.save(deduction);
        return deductionMapper.toResponseDTO(updatedDeduction);
    }

    public void deleteDeduction(Long id) {
        PayrollDeduction deduction = getDeduction(id);
        // Vérification de l'usine avant suppression
        RecyclingCenter center = getCurrentUserRecyclingCenter();
        if (deduction.getEmployee() == null || deduction.getEmployee().getRecyclingCenter() == null ||
                !deduction.getEmployee().getRecyclingCenter().getId().equals(center.getId())) {
            throw new BadRequestException("Cette déduction n'appartient pas à votre usine.");
        }
        deductionRepository.delete(deduction);
    }

    private PayrollDeduction getDeduction(Long id) {
        return deductionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Déduction introuvable avec l'ID : " + id));
    }

    private FactoryUser getAuthenticatedFactoryUser() {
        Long accountId = SecurityUtils.getAccountId()
                .orElseThrow(() -> new UnauthorizedException("Utilisateur non authentifié."));
        return factoryUserRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Employé usine introuvable pour ce compte."));
    }

    private void validateDeduction(PayrollDeduction deduction) {
        if (deduction.getEmployee() == null) {
            throw new BadRequestException("L'employé est obligatoire.");
        }
        if (deduction.getAmount() == null || deduction.getAmount().signum() <= 0) {
            throw new BadRequestException("Le montant doit être supérieur à zéro.");
        }
        if (deduction.getDeductionDate() == null) {
            throw new BadRequestException("La date de déduction est obligatoire.");
        }
    }
}