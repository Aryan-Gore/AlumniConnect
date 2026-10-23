package com.alumniconnect.backend.dto.mentorship;

import com.alumniconnect.backend.dto.common.DepartmentRef;
import com.alumniconnect.backend.entity.MentorshipRequest;
import com.alumniconnect.backend.entity.MentorshipStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class MentorshipResponse {
    private Long id;
    private String message;
    private MentorshipStatus status;
    private Instant requestedAt;
    private Instant respondedAt;
    private StudentSummary student;
    private AlumniSummary alumni;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class StudentSummary {
        private Long id;
        private String fullName;
        private String profilePhotoUrl;
        private DepartmentRef department;
        private String batch;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class AlumniSummary {
        private Long id;
        /** The alumnus' login account, so the UI can offer "Message" once a request is accepted. */
        private Long userId;
        private String fullName;
        private String profilePhotoUrl;
        private String jobTitle;
        private String currentCompany;
    }

    public static MentorshipResponse from(MentorshipRequest r, StudentSummary student) {
        return MentorshipResponse.builder()
                .id(r.getId())
                .message(r.getMessage())
                .status(r.getStatus())
                .requestedAt(r.getRequestedAt())
                .respondedAt(r.getRespondedAt())
                .student(student)
                .alumni(AlumniSummary.builder()
                        .id(r.getAlumni().getId())
                        .userId(r.getAlumni().getUser() != null ? r.getAlumni().getUser().getId() : null)
                        .fullName(r.getAlumni().getFullName())
                        .profilePhotoUrl(r.getAlumni().getUser() != null ? r.getAlumni().getUser().getProfilePhotoUrl() : null)
                        .jobTitle(r.getAlumni().getJobTitle())
                        .currentCompany(r.getAlumni().getCurrentCompany())
                        .build())
                .build();
    }
}
