package com.alumniconnect.backend.dto.auth;

import com.alumniconnect.backend.dto.common.DepartmentRef;
import com.alumniconnect.backend.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * The signed-in user as the frontend keeps them in its auth context. Built by
 * UserSummaryService.currentUser(...) because the photo, department and
 * alumni id live on other tables.
 */
@Getter
@Builder
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private Role role;
    private String profilePhotoUrl;
    /** Null for admins, who don't belong to a department. */
    private DepartmentRef department;
    /** The linked alumni_records id for ALUMNI users; null otherwise. */
    private Long alumniId;
    private boolean verified;
}
