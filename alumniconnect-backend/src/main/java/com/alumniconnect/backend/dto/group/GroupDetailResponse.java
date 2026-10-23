package com.alumniconnect.backend.dto.group;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GroupDetailResponse {
    private Long departmentId;
    private String name;
    private String code;
    private Long instituteId;
    private String instituteCode;
    private String instituteName;
    private long memberCount;
    private long studentCount;
    private long alumniCount;
    private boolean member;
    /** The id of this department's STOMP room; null when viewer is not a member and not an admin. */
    private Long conversationId;
}
