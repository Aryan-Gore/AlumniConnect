package com.alumniconnect.backend.dto.admin;

import com.alumniconnect.backend.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * One row in the admin user table.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserRowResponse {
    private Long id;
    private String fullName;
    private String email;
    private Role role;
    private boolean active;
    /** Photo URL (nullable) */
    private String profilePhotoUrl;
    /** For alumni: department name; for students: department name; null for admins. */
    private String departmentName;
    private Instant createdAt;
}
