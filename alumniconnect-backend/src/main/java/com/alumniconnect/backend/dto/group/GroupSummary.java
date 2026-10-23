package com.alumniconnect.backend.dto.group;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GroupSummary {
    private Long departmentId;
    private String name;
    private String code;
    private Long instituteId;
    private String instituteCode;
    private long memberCount;
    private long studentCount;
    private long alumniCount;
    private boolean member;
}
