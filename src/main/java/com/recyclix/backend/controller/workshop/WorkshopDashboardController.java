package com.recyclix.backend.controller.workshop;

import com.recyclix.backend.service.workshop.WorkshopDashboardService;
import com.recyclix.backend.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workshop/dashboard")
@RequiredArgsConstructor
@PreAuthorize("@factoryAccess.hasPosition('MANAGER')")
public class WorkshopDashboardController {

    private final WorkshopDashboardService workshopDashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<WorkshopDashboardService.WorkshopDashboardResponse>> getDashboard() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Dashboard du chef d’atelier récupéré avec succès.",
                        workshopDashboardService.getDashboard()
                )
        );
    }
}