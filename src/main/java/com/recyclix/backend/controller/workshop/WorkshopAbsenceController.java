package com.recyclix.backend.controller.workshop;

import com.recyclix.backend.dto.employee.EmployeeSummaryDTO;
import com.recyclix.backend.dto.worker_absence.WorkerAbsenceRequestDTO;
import com.recyclix.backend.dto.worker_absence.WorkerAbsenceResponseDTO;
import com.recyclix.backend.dto.worker_absence.WorkerAbsenceSummaryDTO;
import com.recyclix.backend.dto.worker_absence.WorkerAbsenceUpdateDTO;
import com.recyclix.backend.service.hr.EmployeeService;
import com.recyclix.backend.service.hr.WorkerAbsenceService;
import com.recyclix.backend.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workshop/absences")
@RequiredArgsConstructor
@PreAuthorize("@factoryAccess.hasPosition('MANAGER')")
public class WorkshopAbsenceController {

    private final WorkerAbsenceService workerAbsenceService;
    private final EmployeeService employeeService;  // Ajouter cette injection

    // ============ Employés (accessible au MANAGER) ============
    @GetMapping("/employees")
    public ApiResponse<List<EmployeeSummaryDTO>> getAllEmployees() {
        return ApiResponse.ok("Liste des employés récupérée.", employeeService.getAllEmployees());
    }

    // ============ Absences ============
    @PostMapping
    public ApiResponse<WorkerAbsenceResponseDTO> reportAbsence(@Valid @RequestBody WorkerAbsenceRequestDTO request) {
        WorkerAbsenceResponseDTO response = workerAbsenceService.reportAbsence(request);
        return ApiResponse.ok("Absence déclarée avec succès.", response);
    }

    @GetMapping
    public ApiResponse<List<WorkerAbsenceSummaryDTO>> getAllAbsences() {
        return ApiResponse.ok("Liste des absences récupérée.", workerAbsenceService.getAllAbsences());
    }

    @GetMapping("/today")
    public ApiResponse<List<WorkerAbsenceSummaryDTO>> getAbsencesForToday() {
        return ApiResponse.ok("Absences du jour récupérées.", workerAbsenceService.getAbsencesForToday());
    }

    @GetMapping("/employee/{employeeId}")
    public ApiResponse<List<WorkerAbsenceSummaryDTO>> getAbsencesByEmployee(@PathVariable Long employeeId) {
        return ApiResponse.ok("Absences de l'employé récupérées.", workerAbsenceService.getAbsencesByEmployee(employeeId));
    }

    @PutMapping("/{id}")
    public ApiResponse<WorkerAbsenceResponseDTO> updateAbsence(
            @PathVariable Long id,
            @Valid @RequestBody WorkerAbsenceUpdateDTO request) {
        WorkerAbsenceResponseDTO response = workerAbsenceService.updateAbsence(id, request);
        return ApiResponse.ok("Absence mise à jour avec succès.", response);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAbsence(@PathVariable Long id) {
        workerAbsenceService.deleteAbsence(id);
        return ApiResponse.okMessage("Absence supprimée avec succès.");
    }
}