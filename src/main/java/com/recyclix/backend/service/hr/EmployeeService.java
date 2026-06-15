package com.recyclix.backend.service.hr;

import com.recyclix.backend.dto.employee.EmployeeRequestDTO;
import com.recyclix.backend.dto.employee.EmployeeResponseDTO;
import com.recyclix.backend.dto.employee.EmployeeSummaryDTO;
import com.recyclix.backend.dto.employee.EmployeeUpdateDTO;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.exception.UnauthorizedException;
import com.recyclix.backend.mapper.EmployeeMapper;
import com.recyclix.backend.model.Employee;
import com.recyclix.backend.model.FactoryUser;
import com.recyclix.backend.model.RecyclingCenter;
import com.recyclix.backend.repository.EmployeeRepository;
import com.recyclix.backend.repository.FactoryUserRepository;
import com.recyclix.backend.repository.PayrollDeductionRepository;
import com.recyclix.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final PayrollDeductionRepository deductionRepository;
    private final FactoryUserRepository factoryUserRepository;

//    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO request) {
//        if (request.getPhone() != null &&
//                employeeRepository.existsByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndPhone(
//                        request.getFirstName().trim(),
//                        request.getLastName().trim(),
//                        request.getPhone().trim()
//                )) {
//            throw new BadRequestException("Cet employé existe déjà.");
//        }
//
//        Employee employee = employeeMapper.toEntity(request);
//        employee.setFirstName(request.getFirstName().trim());
//        employee.setLastName(request.getLastName().trim());
//
//        if (request.getPhone() != null) {
//            employee.setPhone(request.getPhone().trim());
//        }
//
//        employee.setActive(true);
//
//        return employeeMapper.toResponseDTO(employeeRepository.save(employee));
//    }

    // Méthode paginée (utilisée par AccountantWebController)
    // Méthode paginée
//    @Transactional(readOnly = true)
//    public Page<EmployeeSummaryDTO> getAllEmployees(Pageable pageable) {
//        Page<Employee> page = employeeRepository.findAll(pageable);
//        return page.map(employee -> {
//            BigDecimal totalDeductions = deductionRepository.sumDeductionsByEmployeeId(employee.getId());
//            BigDecimal netSalary = employee.getSalaryAmount().subtract(totalDeductions);
//            EmployeeSummaryDTO dto = employeeMapper.toSummaryDTO(employee);
//            dto.setTotalDeductions(totalDeductions);
//            dto.setNetSalary(netSalary);
//            return dto;
//        });
//    }

//    // Méthode sans pagination (utilisée pour les select)
//    @Transactional(readOnly = true)
//    public List<EmployeeSummaryDTO> getAllEmployees() {
//        List<Employee> employees = employeeRepository.findAll();
//        return employees.stream().map(employee -> {
//            BigDecimal totalDeductions = deductionRepository.sumDeductionsByEmployeeId(employee.getId());
//            BigDecimal netSalary = employee.getSalaryAmount().subtract(totalDeductions);
//            EmployeeSummaryDTO dto = employeeMapper.toSummaryDTO(employee);
//            dto.setTotalDeductions(totalDeductions);
//            dto.setNetSalary(netSalary);
//            return dto;
//        }).toList();
//    }

    @Transactional(readOnly = true)
    public List<EmployeeSummaryDTO> getActiveEmployees() {
        return employeeRepository.findByActiveTrue()
                .stream()
                .map(employeeMapper::toSummaryDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmployeeResponseDTO getEmployeeById(Long id) {
        Employee employee = getEmployee(id);
        return employeeMapper.toResponseDTO(employee);
    }

    @Transactional(readOnly = true)
    public List<EmployeeSummaryDTO> searchEmployees(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllEmployees();
        }

        String value = keyword.trim();

        return employeeRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(value, value)
                .stream()
                .map(employeeMapper::toSummaryDTO)
                .toList();
    }

    public EmployeeResponseDTO updateEmployee(Long id, EmployeeUpdateDTO request) {
        Employee employee = getEmployee(id);

        employeeMapper.updateEntityFromDTO(request, employee);

        if (employee.getFirstName() != null) {
            employee.setFirstName(employee.getFirstName().trim());
        }

        if (employee.getLastName() != null) {
            employee.setLastName(employee.getLastName().trim());
        }

        if (employee.getPhone() != null) {
            employee.setPhone(employee.getPhone().trim());
        }

        return employeeMapper.toResponseDTO(employeeRepository.save(employee));
    }

    public EmployeeResponseDTO deactivateEmployee(Long id) {
        Employee employee = getEmployee(id);
        employee.setActive(false);
        return employeeMapper.toResponseDTO(employeeRepository.save(employee));
    }

    public EmployeeResponseDTO activateEmployee(Long id) {
        Employee employee = getEmployee(id);
        employee.setActive(true);
        return employeeMapper.toResponseDTO(employeeRepository.save(employee));
    }

    public void deleteEmployee(Long id) {
        Employee employee = getEmployee(id);
        employeeRepository.delete(employee);
    }

    private Employee getEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employé introuvable avec l'ID : " + id));
    }

    private RecyclingCenter getCurrentUserRecyclingCenter() {
        Long accountId = SecurityUtils.getAccountId()
                .orElseThrow(() -> new UnauthorizedException("Non authentifié"));
        FactoryUser factoryUser = factoryUserRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("FactoryUser introuvable"));
        if (factoryUser.getRecyclingCenter() == null) {
            throw new BadRequestException("Aucune usine associée à ce comptable.");
        }
        return factoryUser.getRecyclingCenter();
    }

    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO request) {
        RecyclingCenter center = getCurrentUserRecyclingCenter();
        Employee employee = employeeMapper.toEntity(request);
        employee.setRecyclingCenter(center);
        // ... autres setter
        employee.setWilaya(request.getWilaya());
        return employeeMapper.toResponseDTO(employeeRepository.save(employee));
    }

    public Page<EmployeeSummaryDTO> getAllEmployees(Pageable pageable) {
        RecyclingCenter center = getCurrentUserRecyclingCenter();
        Page<Employee> page = employeeRepository.findByRecyclingCenterId(center.getId(), pageable);
        return page.map(emp -> {
            EmployeeSummaryDTO dto = employeeMapper.toSummaryDTO(emp);
            BigDecimal totalDeductions = deductionRepository.sumDeductionsByEmployeeId(emp.getId());
            dto.setTotalDeductions(totalDeductions);
            dto.setNetSalary(emp.getSalaryAmount().subtract(totalDeductions));
            return dto;
        });
    }

    public List<EmployeeSummaryDTO> getAllEmployees() {
        RecyclingCenter center = getCurrentUserRecyclingCenter();
        return employeeRepository.findByRecyclingCenterId(center.getId())
                .stream()
                .map(emp -> {
                    EmployeeSummaryDTO dto = employeeMapper.toSummaryDTO(emp);
                    BigDecimal totalDeductions = deductionRepository.sumDeductionsByEmployeeId(emp.getId());
                    dto.setTotalDeductions(totalDeductions);
                    dto.setNetSalary(emp.getSalaryAmount().subtract(totalDeductions));
                    return dto;
                }).toList();
    }
}