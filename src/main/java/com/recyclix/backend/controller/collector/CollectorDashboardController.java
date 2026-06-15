package com.recyclix.backend.controller.collector;

import com.recyclix.backend.service.collector.CollectorDashboardService;
import com.recyclix.backend.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/collector/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('COLLECTOR')")
public class CollectorDashboardController {

    private final CollectorDashboardService collectorDashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<CollectorDashboardService.CollectorDashboardResponse>> getDashboard() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Dashboard collecteur récupéré avec succès.",
                        collectorDashboardService.getDashboard()
                )
        );
    }
}