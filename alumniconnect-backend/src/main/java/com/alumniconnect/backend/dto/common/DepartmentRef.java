package com.alumniconnect.backend.dto.common;

import com.alumniconnect.backend.entity.Department;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * A department together with the institute it belongs to — the one shape
 * every API uses when it needs to point at a department, so the frontend
 * can always render "CSE · Shri Vaishnav Institute of Technology &amp; Science"
 * without a second request.
 */
@Getter
@Builder
@AllArgsConstructor
public class DepartmentRef {
    private Long id;
    private String name;
    private String code;
    private Long instituteId;
    private String instituteName;
    private String instituteCode;

    /** Reads the lazy {@code institute} association, so call it inside a transaction. */
    public static DepartmentRef from(Department d) {
        if (d == null) return null;
        return DepartmentRef.builder()
                .id(d.getId())
                .name(d.getName())
                .code(d.getCode())
                .instituteId(d.getInstitute().getId())
                .instituteName(d.getInstitute().getName())
                .instituteCode(d.getInstitute().getCode())
                .build();
    }
}
