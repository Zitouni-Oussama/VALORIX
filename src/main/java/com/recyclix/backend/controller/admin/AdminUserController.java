package com.recyclix.backend.controller.admin;

import com.recyclix.backend.dto.account.AccountResponseDTO;
import com.recyclix.backend.dto.account.AccountSummaryDTO;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.mapper.AccountMapper;
import com.recyclix.backend.model.Account;
import com.recyclix.backend.repository.AccountRepository;
import com.recyclix.backend.service.admin.AdminUserService;
import com.recyclix.backend.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("@factoryAccess.hasPosition('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final AccountMapper accountMapper;
    private final AccountRepository accountRepository;

    @GetMapping
    public ApiResponse<Page<AccountSummaryDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(
                "Liste des utilisateurs récupérée avec succès.",
                adminUserService.getAllUsers(page, size)
        );
    }

    @GetMapping("/search")
    public ApiResponse<Page<AccountSummaryDTO>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(
                "Recherche des utilisateurs effectuée avec succès.",
                adminUserService.searchUsers(keyword, page, size)
        );
    }

    @GetMapping("/role/{roleType}")
    public ApiResponse<Page<AccountSummaryDTO>> getUsersByRole(
            @PathVariable Account.RoleType roleType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(
                "Utilisateurs récupérés par rôle avec succès.",
                adminUserService.getUsersByRole(roleType, page, size)
        );
    }

    @GetMapping("/status/{status}")
    public ApiResponse<Page<AccountSummaryDTO>> getUsersByStatus(
            @PathVariable Account.AccountStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(
                "Utilisateurs récupérés par statut avec succès.",
                adminUserService.getUsersByStatus(status, page, size)
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<AccountResponseDTO> getUserById(@PathVariable Long id) {
        return ApiResponse.ok(
                "Utilisateur récupéré avec succès.",
                adminUserService.getUserById(id)
        );
    }

    @GetMapping("/stats")
    public ApiResponse<AdminUserService.UserStatsResponse> getUserStats() {
        return ApiResponse.ok(
                "Statistiques utilisateurs récupérées avec succès.",
                adminUserService.getUserStats()
        );
    }

    @PutMapping("/{id}/activate")
    public ApiResponse<AccountResponseDTO> activateUser(@PathVariable Long id) {
        return ApiResponse.ok(
                "Utilisateur activé avec succès.",
                adminUserService.activateUser(id)
        );
    }

    @PutMapping("/{id}/deactivate")
    public ApiResponse<AccountResponseDTO> deactivateUser(@PathVariable Long id) {
        return ApiResponse.ok(
                "Utilisateur désactivé avec succès.",
                adminUserService.deactivateUser(id)
        );
    }

    @PutMapping("/{id}/block")
    public ApiResponse<AccountResponseDTO> blockUser(@PathVariable Long id) {
        return ApiResponse.ok(
                "Utilisateur bloqué avec succès.",
                adminUserService.blockUser(id)
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> softDeleteUser(@PathVariable Long id) {
        adminUserService.softDeleteUser(id);
        return ApiResponse.okMessage("Utilisateur supprimé logiquement avec succès.");
    }

    // Dans AdminUserController.java

    @PutMapping("/{id}/reactivate")
    @PreAuthorize("@factoryAccess.hasPosition('ADMIN')")
    public ApiResponse<AccountResponseDTO> reactivateAccount(@PathVariable Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable"));
        if (account.getStatus() != Account.AccountStatus.INACTIVE) {
            throw new BadRequestException("Seul un compte inactif peut être réactivé");
        }
        account.setStatus(Account.AccountStatus.ACTIVE);
        accountRepository.save(account);
        return ApiResponse.ok("Compte réactivé avec succès", accountMapper.toDto(account));
    }
}