package com.alumniconnect.backend.service;

import com.alumniconnect.backend.dto.admin.AdminUserRowResponse;
import com.alumniconnect.backend.entity.AuditAction;
import com.alumniconnect.backend.entity.NotificationCategory;
import com.alumniconnect.backend.entity.Role;
import com.alumniconnect.backend.entity.User;
import com.alumniconnect.backend.exception.BadRequestException;
import com.alumniconnect.backend.exception.ResourceNotFoundException;
import com.alumniconnect.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin user management: list, activate, deactivate.
 * Admins cannot deactivate themselves or other admins.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;
    private final UserSummaryService userSummaryService;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    // -----------------------------------------------------------------------
    // List
    // -----------------------------------------------------------------------

    public Page<AdminUserRowResponse> listUsers(String query, Role role, int page, int size) {
        String q = (query != null && !query.isBlank()) ? query.trim() : null;
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(size <= 0 ? 25 : size, 100));
        Page<User> users = userRepository.findAdminUsers(role, q, pageable);
        return users.map(this::toRow);
    }

    // -----------------------------------------------------------------------
    // Status toggle
    // -----------------------------------------------------------------------

    /**
     * Activate or deactivate a user account.
     * Admins may not change another admin's status.
     * Caller (admin) may not change their own status.
     */
    @Transactional
    public AdminUserRowResponse setStatus(User admin, Long targetId, boolean active) {
        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        if (target.getRole() == Role.ADMIN) {
            throw new BadRequestException("Admin accounts cannot be deactivated through this endpoint.");
        }
        if (target.getId().equals(admin.getId())) {
            throw new BadRequestException("You cannot change your own status.");
        }
        if (target.isActive() == active) {
            return toRow(target); // no-op, idempotent
        }
        target.setActive(active);
        userRepository.save(target);

        AuditAction action = active ? AuditAction.REACTIVATE_USER : AuditAction.DEACTIVATE_USER;
        auditLogService.record(admin, action,
                (active ? "Reactivated" : "Deactivated") + " user id=" + targetId
                        + " (" + target.getEmail() + ")");

        if (!active) {
            notificationService.notify(target, NotificationCategory.ACCOUNT,
                    "Your account has been deactivated by an administrator.");
        } else {
            notificationService.notify(target, NotificationCategory.ACCOUNT,
                    "Your account has been reactivated.");
        }

        return toRow(target);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private AdminUserRowResponse toRow(User u) {
        // summarize() resolves department name via AlumniRecord / StudentProfile
        var summary = userSummaryService.summarize(u);
        String departmentName = summary.getDepartment() != null
                ? summary.getDepartment().getName()
                : null;

        return AdminUserRowResponse.builder()
                .id(u.getId())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .role(u.getRole())
                .active(u.isActive())
                .profilePhotoUrl(u.getProfilePhotoUrl())
                .departmentName(departmentName)
                .createdAt(u.getCreatedAt())
                .build();
    }
}
