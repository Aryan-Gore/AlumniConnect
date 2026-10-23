package com.alumniconnect.backend.dto.auth;

import com.alumniconnect.backend.dto.common.DepartmentRef;
import com.alumniconnect.backend.entity.AlumniRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ClaimLookupResponse {
    private Long id;
    private String fullName;
    private String enrollmentNumber;
    private DepartmentRef department;
    private Integer graduationYear;

    /** Reads the lazy department, so call it inside a transaction. */
    public static ClaimLookupResponse from(AlumniRecord record) {
        return ClaimLookupResponse.builder()
                .id(record.getId())
                .fullName(record.getFullName())
                .enrollmentNumber(record.getEnrollmentNumber())
                .department(DepartmentRef.from(record.getDepartment()))
                .graduationYear(record.getGraduationYear())
                .build();
    }
}
