package com.recyclix.backend.controller.workshop;

import com.recyclix.backend.model.Machine;
import com.recyclix.backend.service.workshop.WorkshopMaintenanceService;
import com.recyclix.backend.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workshop/maintenance")
@RequiredArgsConstructor
@PreAuthorize("@factoryAccess.hasPosition('MANAGER')")
public class WorkshopMaintenanceController {

    private final WorkshopMaintenanceService workshopMaintenanceService;

    @GetMapping("/machines")
    public ResponseEntity<ApiResponse<List<WorkshopMaintenanceService.MachineSummaryResponse>>> getAllMachines() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Machines récupérées avec succès.",
                        workshopMaintenanceService.getAllMachines()
                )
        );
    }

    @GetMapping("/machines/{machineId}")
    public ResponseEntity<ApiResponse<WorkshopMaintenanceService.MachineDetailResponse>> getMachineById(
            @PathVariable Long machineId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Détail de la machine récupéré avec succès.",
                        workshopMaintenanceService.getMachineById(machineId)
                )
        );
    }

    @PutMapping("/machines/{machineId}/status")
    public ResponseEntity<ApiResponse<WorkshopMaintenanceService.MachineDetailResponse>> updateMachineStatus(
            @PathVariable Long machineId,
            @Valid @RequestBody WorkshopMaintenanceService.UpdateMachineStatusRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Statut de la machine mis à jour avec succès.",
                        workshopMaintenanceService.updateMachineStatus(machineId, request)
                )
        );
    }

    @GetMapping("/incidents")
    public ResponseEntity<ApiResponse<List<WorkshopMaintenanceService.IncidentSummaryResponse>>> getAllIncidents() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Incidents récupérés avec succès.",
                        workshopMaintenanceService.getAllIncidents()
                )
        );
    }

    @GetMapping("/incidents/open")
    public ResponseEntity<ApiResponse<List<WorkshopMaintenanceService.IncidentSummaryResponse>>> getOpenIncidents() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Incidents ouverts récupérés avec succès.",
                        workshopMaintenanceService.getOpenIncidents()
                )
        );
    }

    @GetMapping("/incidents/{incidentId}")
    public ResponseEntity<ApiResponse<WorkshopMaintenanceService.IncidentDetailResponse>> getIncidentById(
            @PathVariable Long incidentId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Détail de l'incident récupéré avec succès.",
                        workshopMaintenanceService.getIncidentById(incidentId)
                )
        );
    }

    @PostMapping("/incidents")
    public ResponseEntity<ApiResponse<WorkshopMaintenanceService.IncidentDetailResponse>> createIncident(
            @RequestPart("request") @Valid WorkshopMaintenanceService.CreateIncidentRequest request,
            @RequestPart(value = "incidentImage", required = false) MultipartFile incidentImage
    ) {
        WorkshopMaintenanceService.IncidentDetailResponse response =
                workshopMaintenanceService.createIncident(request, incidentImage);
        return ResponseEntity.ok(
                ApiResponse.ok("Incident créé avec succès.", response)
        );
    }

    @PutMapping("/incidents/{incidentId}/resolve")
    public ResponseEntity<ApiResponse<WorkshopMaintenanceService.IncidentDetailResponse>> resolveIncident(
            @PathVariable Long incidentId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Incident résolu avec succès.",
                        workshopMaintenanceService.resolveIncident(incidentId)
                )
        );
    }

    @PutMapping("/incidents/{incidentId}/status")
    public ResponseEntity<ApiResponse<WorkshopMaintenanceService.IncidentDetailResponse>> updateIncidentStatus(
            @PathVariable Long incidentId,
            @RequestBody Map<String, String> statusUpdate
    ) {
        String newStatus = statusUpdate.get("status");
        WorkshopMaintenanceService.IncidentDetailResponse response =
                workshopMaintenanceService.updateIncidentStatus(incidentId, newStatus);
        return ResponseEntity.ok(
                ApiResponse.ok("Statut de l'incident mis à jour avec succès.", response)
        );
    }
}