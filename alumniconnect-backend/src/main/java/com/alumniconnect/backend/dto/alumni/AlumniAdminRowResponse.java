package com.alumniconnect.backend.dto.alumni;

import com.alumniconnect.backend.dto.common.DepartmentRef;
import com.alumniconnect.backend.entity.AlumniStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/** A row in the admin alumni-management table: the institution's record, plus where it is in the claim workflow. */
@Getter
@Builder
@AllArgsConstructor
public class AlumniAdminRowResponse {
    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String enrollmentNumber;
    private String degree;
    private DepartmentRef department;
    private String batch;
    private Integer graduationYear;
    private AlumniStatus status;
    private boolean verified;
    private String currentCompany;
    private String jobTitle;
    private Instant invitedAt;
    private Instant claimedAt;
}
