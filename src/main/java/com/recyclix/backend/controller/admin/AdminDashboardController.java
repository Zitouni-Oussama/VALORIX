package com.recyclix.backend.controller.admin;

import com.recyclix.backend.service.admin.AdminDashboardService;
import com.recyclix.backend.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("@factoryAccess.hasPosition('ADMIN')")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<AdminDashboardService.AdminDashboardResponse>> getDashboard() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Dashboard admin récupéré avec succès.",
                        adminDashboardService.getDashboard()
                )
        );
    }
}