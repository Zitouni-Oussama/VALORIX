package com.recyclix.backend.controller.hr;

import com.recyclix.backend.dto.worker_absence.WorkerAbsenceRequestDTO;
import com.recyclix.backend.dto.worker_absence.WorkerAbsenceResponseDTO;
import com.recyclix.backend.dto.worker_absence.WorkerAbsenceSummaryDTO;
import com.recyclix.backend.dto.worker_absence.WorkerAbsenceUpdateDTO;
import com.recyclix.backend.service.hr.WorkerAbsenceService;
import com.recyclix.backend.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/absences")
@PreAuthorize("@factoryAccess.hasPosition('ACCOUNTANT')")
@RequiredArgsConstructor
public class WorkerAbsenceController {

    private final WorkerAbsenceService absenceService;

    // 1. Déclarer une absence
    @PostMapping
    public ApiResponse<WorkerAbsenceResponseDTO> reportAbsence(@Valid @RequestBody WorkerAbsenceRequestDTO request) {
        WorkerAbsenceResponseDTO response = absenceService.reportAbsence(request);
        return ApiResponse.ok("Absence déclarée avec succès.", response);
    }

    // 2. Voir toutes les absences
    @GetMapping
    public ApiResponse<List<WorkerAbsenceSummaryDTO>> getAllAbsences() {
        return ApiResponse.ok("Liste des absences récupérée.", absenceService.getAllAbsences());
    }

    // 3. Voir les absents/retards d'aujourd'hui
    @GetMapping("/today")
    public ApiResponse<List<WorkerAbsenceSummaryDTO>> getAbsencesForToday() {
        return ApiResponse.ok("Absences du jour récupérées.", absenceService.getAbsencesForToday());
    }

    // 4. Mettre à jour une absence
    @PutMapping("/{id}")
    public ApiResponse<WorkerAbsenceResponseDTO> updateAbsence(
            @PathVariable Long id,
            @Valid @RequestBody WorkerAbsenceUpdateDTO request) {
        WorkerAbsenceResponseDTO response = absenceService.updateAbsence(id, request);
        return ApiResponse.ok("Absence mise à jour avec succès.", response);
    }

    // 5. Supprimer une absence
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAbsence(@PathVariable Long id) {
        absenceService.deleteAbsence(id);
        return ApiResponse.okMessage("L'absence a été supprimée des registres.");
    }

    @GetMapping("/employee/{employeeId}")
    public ApiResponse<List<WorkerAbsenceSummaryDTO>> getAbsencesByEmployee(@PathVariable Long employeeId) {
        return ApiResponse.ok(
                "Absences de l'employé récupérées avec succès.",
                absenceService.getAbsencesByEmployee(employeeId)
        );
    }
}