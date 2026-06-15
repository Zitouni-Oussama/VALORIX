package com.recyclix.backend.controller.accountant;

import com.recyclix.backend.dto.account.AccountingDashboardDTO;
import com.recyclix.backend.service.accountant.AccountingDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accountant/dashboard")
@PreAuthorize("@factoryAccess.hasPosition('ACCOUNTANT')")
@RequiredArgsConstructor
public class AccountingDashboardController {

    private final AccountingDashboardService dashboardService;

    @GetMapping
    public ResponseEntity<AccountingDashboardDTO> getDashboard() {
        AccountingDashboardDTO metrics = dashboardService.getDashboardMetrics();
        return ResponseEntity.ok(metrics);
    }
}