package com.alumniconnect.backend.dto.job;

import com.alumniconnect.backend.dto.common.UserSummaryResponse;
import com.alumniconnect.backend.entity.ApplicationStatus;
import com.alumniconnect.backend.entity.JobApplication;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class JobApplicationResponse {

    private Long id;
    private Long jobId;
    private String jobTitle;
    private String company;
    private String applicantName;
    /** Filled in only for the poster's "who applied" list, so they can open the profile or start a chat. */
    private UserSummaryResponse applicant;
    private ApplicationStatus status;
    private Instant appliedAt;

    public static JobApplicationResponse from(JobApplication app) {
        return from(app, null);
    }

    public static JobApplicationResponse from(JobApplication app, UserSummaryResponse applicantSummary) {
        return JobApplicationResponse.builder()
                .id(app.getId())
                .jobId(app.getJob().getId())
                .jobTitle(app.getJob().getTitle())
                .company(app.getJob().getCompany())
                .applicantName(app.getApplicant().getFullName())
                .applicant(applicantSummary)
                .status(app.getStatus())
                .appliedAt(app.getAppliedAt())
                .build();
    }
}
