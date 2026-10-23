package com.alumniconnect.backend.dto.common;

import com.alumniconnect.backend.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * The compact "who is this?" block used wherever a person appears next to
 * content: chat participants and senders, feed authors, comment authors,
 * mentorship and job cards. Always built through UserSummaryService so the
 * headline and department are resolved the same way everywhere.
 */
@Getter
@Builder
@AllArgsConstructor
public class UserSummaryResponse {
    private Long id;
    private String fullName;
    private Role role;
    private String profilePhotoUrl;
    /** "Software Engineer at Acme", "B.Tech CSE · Batch 2026", or "Administrator". */
    private String headline;
    private DepartmentRef department;
    /** The alumni_records id for ALUMNI users (links to /alumni/:id); null otherwise. */
    private Long alumniId;
    /** True for alumni whose record an admin has verified. */
    private boolean verified;
}
