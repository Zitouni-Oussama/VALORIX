package com.recyclix.backend.controller.admin;

import com.recyclix.backend.dto.factory_user.FactoryUserResponseDTO;
import com.recyclix.backend.model.FactoryUser;
import com.recyclix.backend.service.admin.AdminFactoryUserService;
import com.recyclix.backend.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/factory-users")
@RequiredArgsConstructor
@PreAuthorize("@factoryAccess.hasPosition('ADMIN')")
public class AdminFactoryUserController {

    private final AdminFactoryUserService adminFactoryUserService;

//    @PostMapping("/accountant")
//    public ApiResponse<FactoryUserResponseDTO> createAccountant(
//            @Valid @RequestBody AdminFactoryUserService.CreateFactoryUserRequest request
//    ) {
//        return ApiResponse.ok(
//                "Compte comptable créé avec succès.",
//                adminFactoryUserService.createAccountant(request)
//        );
//    }

//    @PostMapping("/workshop-manager")
//    public ApiResponse<FactoryUserResponseDTO> createWorkshopManager(
//            @Valid @RequestBody AdminFactoryUserService.CreateFactoryUserRequest request
//    ) {
//        return ApiResponse.ok(
//                "Compte chef d’atelier créé avec succès.",
//                adminFactoryUserService.createWorkshopManager(request)
//        );
//    }

    @GetMapping
    public ApiResponse<Page<FactoryUserResponseDTO>> getAllFactoryUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(
                "Employés usine récupérés avec succès.",
                adminFactoryUserService.getAllFactoryUsers(page, size)
        );
    }

    @GetMapping("/position/{position}")
    public ApiResponse<Page<FactoryUserResponseDTO>> getFactoryUsersByPosition(
            @PathVariable FactoryUser.FactoryPosition position,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(
                "Employés usine récupérés par position avec succès.",
                adminFactoryUserService.getFactoryUsersByPosition(position, page, size)
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<FactoryUserResponseDTO> getFactoryUserById(@PathVariable Long id) {
        return ApiResponse.ok(
                "Employé usine récupéré avec succès.",
                adminFactoryUserService.getFactoryUserById(id)
        );
    }

    @PutMapping("/{id}")
    public ApiResponse<FactoryUserResponseDTO> updateFactoryUser(
            @PathVariable Long id,
            @RequestBody AdminFactoryUserService.UpdateFactoryUserRequest request
    ) {
        return ApiResponse.ok(
                "Employé usine modifié avec succès.",
                adminFactoryUserService.updateFactoryUser(id, request)
        );
    }

    @PutMapping("/{id}/activate")
    public ApiResponse<FactoryUserResponseDTO> activateFactoryUser(@PathVariable Long id) {
        return ApiResponse.ok(
                "Employé usine activé avec succès.",
                adminFactoryUserService.activateFactoryUser(id)
        );
    }

    @PutMapping("/{id}/deactivate")
    public ApiResponse<FactoryUserResponseDTO> deactivateFactoryUser(@PathVariable Long id) {
        return ApiResponse.ok(
                "Employé usine désactivé avec succès.",
                adminFactoryUserService.deactivateFactoryUser(id)
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> softDeleteFactoryUser(@PathVariable Long id) {
        adminFactoryUserService.softDeleteFactoryUser(id);
        return ApiResponse.okMessage("Employé usine supprimé logiquement avec succès.");
    }
}