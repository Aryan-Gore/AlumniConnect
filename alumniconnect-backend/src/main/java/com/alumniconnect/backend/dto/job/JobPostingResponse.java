package com.alumniconnect.backend.dto.job;

import com.alumniconnect.backend.dto.common.UserSummaryResponse;
import com.alumniconnect.backend.entity.JobPosting;
import com.alumniconnect.backend.entity.JobStatus;
import com.alumniconnect.backend.entity.JobType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class JobPostingResponse {

    private Long id;
    private String title;
    private String company;
    private String location;
    private JobType jobType;
    private String description;
    private JobStatus status;
    private Instant postedAt;
    private LocalDate expiresAt;
    /** Who posted it — name, photo, headline, verified badge, and the ids needed to open their profile or message them. */
    private UserSummaryResponse postedBy;
    /** True when the viewer posted this listing (the UI then offers Edit and Applicants). */
    private boolean ownedByViewer;
    private boolean hasAppliedByViewer;

    public static JobPostingResponse from(JobPosting job, UserSummaryResponse poster, Long viewerUserId, boolean hasAppliedByViewer) {
        return JobPostingResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .company(job.getCompany())
                .location(job.getLocation())
                .jobType(job.getJobType())
                .description(job.getDescription())
                .status(job.getStatus())
                .postedAt(job.getPostedAt())
                .expiresAt(job.getExpiresAt())
                .postedBy(poster)
                .ownedByViewer(viewerUserId != null && viewerUserId.equals(job.getPostedBy().getId()))
                .hasAppliedByViewer(hasAppliedByViewer)
                .build();
    }
}
