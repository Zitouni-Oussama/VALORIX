package com.recyclix.backend.controller.hr;

import com.recyclix.backend.dto.payroll.DeductionRequestDTO;
import com.recyclix.backend.dto.payroll.DeductionResponseDTO;
import com.recyclix.backend.dto.payroll.DeductionSummaryDTO;
import com.recyclix.backend.dto.payroll.DeductionUpdateDTO;
import com.recyclix.backend.service.hr.PayrollDeductionService;
import com.recyclix.backend.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/deductions")
@PreAuthorize("@factoryAccess.hasPosition('ACCOUNTANT')")
@RequiredArgsConstructor
public class PayrollDeductionController {

    private final PayrollDeductionService deductionService;

    @PostMapping
    public ApiResponse<DeductionResponseDTO> createDeduction(
            @Valid @RequestBody DeductionRequestDTO request
    ) {
        return ApiResponse.ok(
                "Déduction appliquée avec succès.",
                deductionService.addDeduction(request)
        );
    }

    @GetMapping
    public ApiResponse<List<DeductionSummaryDTO>> getAllDeductions() {
        return ApiResponse.ok(
                "Liste des retenues sur salaire récupérée.",
                deductionService.getAllDeductions()
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<DeductionResponseDTO> getDeductionById(@PathVariable Long id) {
        return ApiResponse.ok(
                "Déduction récupérée avec succès.",
                deductionService.getDeductionById(id)
        );
    }

    @GetMapping("/employee/{employeeId}")
    public ApiResponse<List<DeductionSummaryDTO>> getDeductionsByEmployee(@PathVariable Long employeeId) {
        return ApiResponse.ok(
                "Historique des retenues sur salaire récupéré.",
                deductionService.getDeductionsByEmployee(employeeId)
        );
    }

    @GetMapping("/monthly")
    public ApiResponse<List<DeductionSummaryDTO>> getMonthlyDeductions(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ApiResponse.ok(
                "Déductions du mois récupérées pour la comptabilité.",
                deductionService.getMonthlyDeductions(year, month)
        );
    }

    @PutMapping("/{id}")
    public ApiResponse<DeductionResponseDTO> updateDeduction(
            @PathVariable Long id,
            @Valid @RequestBody DeductionUpdateDTO request
    ) {
        return ApiResponse.ok(
                "Déduction mise à jour avec succès.",
                deductionService.updateDeduction(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDeduction(@PathVariable Long id) {
        deductionService.deleteDeduction(id);
        return ApiResponse.okMessage("La déduction a été supprimée avec succès.");
    }
}