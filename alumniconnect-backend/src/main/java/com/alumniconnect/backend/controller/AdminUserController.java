package com.alumniconnect.backend.controller;

import com.alumniconnect.backend.dto.admin.AdminUserRowResponse;
import com.alumniconnect.backend.dto.admin.SetUserStatusRequest;
import com.alumniconnect.backend.entity.Role;
import com.alumniconnect.backend.entity.User;
import com.alumniconnect.backend.security.CurrentUser;
import com.alumniconnect.backend.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin user management.
 *
 * GET   /admin/users               — paginated user list (filter by role, query)
 * PATCH /admin/users/{id}/status   — activate / deactivate account
 */
@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<Page<AdminUserRowResponse>> listUsers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Role role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        return ResponseEntity.ok(adminUserService.listUsers(query, role, page, size));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AdminUserRowResponse> setStatus(
            @CurrentUser User admin,
            @PathVariable Long id,
            @Valid @RequestBody SetUserStatusRequest req) {
        return ResponseEntity.ok(adminUserService.setStatus(admin, id, req.getActive()));
    }
}
