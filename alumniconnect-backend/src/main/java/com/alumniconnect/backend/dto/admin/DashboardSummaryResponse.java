package com.alumniconnect.backend.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class DashboardSummaryResponse {
    private long totalAlumni;
    private long activeAlumni;
    private long claimedProfiles;
    private long unclaimedProfiles;
    private long upcomingEvents;
    private long activeMentorships;
    private long publishedJobs;
    private long pendingJobApprovals;
    private List<LabelCountResponse> byDepartment;
    private List<LabelCountResponse> byGraduationYear;
}
