package com.recyclix.backend.controller.hr;

import com.recyclix.backend.dto.employee.EmployeeRequestDTO;
import com.recyclix.backend.dto.employee.EmployeeResponseDTO;
import com.recyclix.backend.dto.employee.EmployeeSummaryDTO;
import com.recyclix.backend.dto.employee.EmployeeUpdateDTO;
import com.recyclix.backend.service.hr.EmployeeService;
import com.recyclix.backend.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/employees")
@PreAuthorize("@factoryAccess.hasPosition('ACCOUNTANT')")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public ApiResponse<EmployeeResponseDTO> createEmployee(
            @Valid @RequestBody EmployeeRequestDTO request
    ) {
        return ApiResponse.ok(
                "Employé ajouté avec succès.",
                employeeService.createEmployee(request)
        );
    }

//    @GetMapping
//    public ApiResponse<List<EmployeeSummaryDTO>> getAllEmployees() {
//        return ApiResponse.ok(
//                "Liste des employés récupérée.",
//                employeeService.getAllEmployees()
//        );
//    }

    @GetMapping("/active")
    public ApiResponse<List<EmployeeSummaryDTO>> getActiveEmployees() {
        return ApiResponse.ok(
                "Liste des employés actifs récupérée.",
                employeeService.getActiveEmployees()
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<EmployeeResponseDTO> getEmployeeById(@PathVariable Long id) {
        return ApiResponse.ok(
                "Employé récupéré avec succès.",
                employeeService.getEmployeeById(id)
        );
    }

    @GetMapping("/search")
    public ApiResponse<List<EmployeeSummaryDTO>> searchEmployees(
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(
                "Recherche des employés effectuée.",
                employeeService.searchEmployees(keyword)
        );
    }

    @PutMapping("/{id}")
    public ApiResponse<EmployeeResponseDTO> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeUpdateDTO request
    ) {
        return ApiResponse.ok(
                "Employé mis à jour avec succès.",
                employeeService.updateEmployee(id, request)
        );
    }

    @PutMapping("/{id}/activate")
    public ApiResponse<EmployeeResponseDTO> activateEmployee(@PathVariable Long id) {
        return ApiResponse.ok(
                "Employé activé avec succès.",
                employeeService.activateEmployee(id)
        );
    }

    @PutMapping("/{id}/deactivate")
    public ApiResponse<EmployeeResponseDTO> deactivateEmployee(@PathVariable Long id) {
        return ApiResponse.ok(
                "Employé désactivé avec succès.",
                employeeService.deactivateEmployee(id)
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ApiResponse.okMessage("Employé supprimé avec succès.");
    }
}