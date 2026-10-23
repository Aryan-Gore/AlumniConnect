package com.alumniconnect.backend.service;

import com.alumniconnect.backend.dto.admin.DashboardSummaryResponse;
import com.alumniconnect.backend.dto.admin.LabelCountResponse;
import com.alumniconnect.backend.entity.AlumniStatus;
import com.alumniconnect.backend.entity.EventStatus;
import com.alumniconnect.backend.entity.JobStatus;
import com.alumniconnect.backend.entity.MentorshipStatus;
import com.alumniconnect.backend.repository.AlumniRecordRepository;
import com.alumniconnect.backend.repository.EventRepository;
import com.alumniconnect.backend.repository.JobPostingRepository;
import com.alumniconnect.backend.repository.MentorshipRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Every figure here is a plain SQL aggregate computed at request time —
 * fine at this data volume. The SRS's Python/Pandas job is for the
 * heavier statistical work (career-growth trends, engagement analysis);
 * nothing here should block on it.
 *
 * The aggregate queries return raw [label, count] rows rather than Spring
 * Data interface projections: a grouped JPQL query with a computed label is
 * exactly where projections get fragile, and a two-column Object[] cannot
 * be mapped wrongly.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final AlumniRecordRepository alumniRecordRepository;
    private final EventRepository eventRepository;
    private final MentorshipRequestRepository mentorshipRequestRepository;
    private final JobPostingRepository jobPostingRepository;

    public DashboardSummaryResponse summary() {
        long total = alumniRecordRepository.count();
        long active = alumniRecordRepository.countByStatus(AlumniStatus.ACTIVE);
        long claimed = alumniRecordRepository.countByStatus(AlumniStatus.CLAIMED) + active;
        long unclaimed = alumniRecordRepository.countByStatus(AlumniStatus.IMPORTED)
                + alumniRecordRepository.countByStatus(AlumniStatus.INVITED);

        return DashboardSummaryResponse.builder()
                .totalAlumni(total)
                .activeAlumni(active)
                .claimedProfiles(claimed)
                .unclaimedProfiles(unclaimed)
                .upcomingEvents(eventRepository.countByStatus(EventStatus.PUBLISHED))
                .activeMentorships(mentorshipRequestRepository.countByStatus(MentorshipStatus.ACCEPTED))
                .publishedJobs(jobPostingRepository.countByStatus(JobStatus.APPROVED))
                .pendingJobApprovals(jobPostingRepository.countByStatus(JobStatus.PENDING))
                .byDepartment(toLabelCounts(alumniRecordRepository.countGroupedByDepartment()))
                .byGraduationYear(toLabelCounts(alumniRecordRepository.countGroupedByGraduationYear()))
                .build();
    }

    public List<LabelCountResponse> byInstitute() {
        return toLabelCounts(alumniRecordRepository.countGroupedByInstitute());
    }

    public List<LabelCountResponse> byIndustry() {
        return toLabelCounts(alumniRecordRepository.countGroupedByIndustry());
    }

    public List<LabelCountResponse> byLocation() {
        return toLabelCounts(alumniRecordRepository.countGroupedByLocation());
    }

    private List<LabelCountResponse> toLabelCounts(List<Object[]> rows) {
        List<LabelCountResponse> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(LabelCountResponse.builder()
                    .label(String.valueOf(row[0]))
                    .value(((Number) row[1]).longValue())
                    .build());
        }
        return result;
    }
}
