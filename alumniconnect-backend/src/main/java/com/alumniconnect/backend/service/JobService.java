package com.alumniconnect.backend.service;

import com.alumniconnect.backend.dto.common.UserSummaryResponse;
import com.alumniconnect.backend.dto.job.JobApplicationResponse;
import com.alumniconnect.backend.dto.job.JobPostingRequest;
import com.alumniconnect.backend.dto.job.JobPostingResponse;
import com.alumniconnect.backend.entity.ApplicationStatus;
import com.alumniconnect.backend.entity.AuditAction;
import com.alumniconnect.backend.entity.JobApplication;
import com.alumniconnect.backend.entity.JobPosting;
import com.alumniconnect.backend.entity.JobStatus;
import com.alumniconnect.backend.entity.NotificationCategory;
import com.alumniconnect.backend.entity.Role;
import com.alumniconnect.backend.entity.User;
import com.alumniconnect.backend.exception.BadRequestException;
import com.alumniconnect.backend.exception.DuplicateResourceException;
import com.alumniconnect.backend.exception.ResourceNotFoundException;
import com.alumniconnect.backend.repository.JobApplicationRepository;
import com.alumniconnect.backend.repository.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Job listings. Anyone signed in can post one — a student passing on an
 * internship lead, an alumnus opening a vacancy, the office posting its own —
 * and every listing except an admin's goes through moderation before the
 * public sees it. Class-level read-only transaction because mapping a
 * listing reads the lazy poster; writers override it.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobService {

    private final JobPostingRepository jobPostingRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final UserSummaryService userSummaryService;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    // ------------------------------------------------------------------
    // Browsing and applying (any signed-in user)
    // ------------------------------------------------------------------

    public List<JobPostingResponse> browse(Long viewerUserId) {
        return toResponses(jobPostingRepository.findOpen(JobStatus.APPROVED, LocalDate.now()), viewerUserId);
    }

    /**
     * Approved, unexpired listings are open to everyone; a listing in any other
     * state (pending, rejected, removed, expired) is visible only to the person
     * who posted it and to admins — everyone else gets a plain 404.
     */
    public JobPostingResponse getById(Long id, User viewer) {
        JobPosting job = findOrThrow(id);
        boolean owner = isOwner(job, viewer.getId());
        boolean admin = viewer.getRole() == Role.ADMIN;
        if (!owner && !admin && !isOpen(job)) {
            throw new ResourceNotFoundException("Job " + id + " not found.");
        }
        return toResponses(List.of(job), viewer.getId()).get(0);
    }

    @Transactional
    public JobApplicationResponse apply(Long jobId, User applicant) {
        if (applicant.getRole() == Role.ADMIN) {
            throw new BadRequestException("Administrator accounts can't apply to listings.");
        }
        JobPosting job = findOrThrow(jobId);
        if (!isOpen(job)) {
            throw new BadRequestException("This listing isn't currently accepting applications.");
        }
        if (isOwner(job, applicant.getId())) {
            throw new BadRequestException("You can't apply to your own listing.");
        }
        if (jobApplicationRepository.existsByJobIdAndApplicantId(jobId, applicant.getId())) {
            throw new DuplicateResourceException("You've already applied to this job.");
        }

        JobApplication application = jobApplicationRepository.save(JobApplication.builder()
                .job(job).applicant(applicant).status(ApplicationStatus.SUBMITTED).build());

        notificationService.notify(job.getPostedBy(), NotificationCategory.JOB,
                applicant.getFullName() + " applied to your \"" + job.getTitle() + "\" listing.");
        notificationService.notify(applicant, NotificationCategory.JOB,
                "Your application to " + job.getTitle() + " was received.");

        return JobApplicationResponse.from(application);
    }

    public List<JobApplicationResponse> myApplications(Long applicantId) {
        return jobApplicationRepository.findByApplicantIdOrderByAppliedAtDesc(applicantId).stream()
                .map(JobApplicationResponse::from)
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------
    // Posting and managing your own listings (any signed-in user)
    // ------------------------------------------------------------------

    @Transactional
    public JobPostingResponse post(JobPostingRequest request, User poster) {
        boolean admin = poster.getRole() == Role.ADMIN;

        JobPosting job = JobPosting.builder()
                .postedBy(poster)
                .title(request.getTitle().trim())
                .company(request.getCompany().trim())
                .location(blankToNull(request.getLocation()))
                .jobType(request.getJobType())
                .description(blankToNull(request.getDescription()))
                .expiresAt(request.getExpiresAt())
                // The office is the moderator; making it wait for its own approval would be silly.
                .status(admin ? JobStatus.APPROVED : JobStatus.PENDING)
                .build();
        if (admin) {
            job.setModeratedBy(poster);
            job.setModeratedAt(Instant.now());
        }
        job = jobPostingRepository.save(job);

        return toResponses(List.of(job), poster.getId()).get(0);
    }

    @Transactional
    public JobPostingResponse update(Long jobId, JobPostingRequest request, User editor) {
        JobPosting job = findOrThrow(jobId);
        if (!isOwner(job, editor.getId())) {
            // Not yours: indistinguishable from a listing that doesn't exist.
            throw new ResourceNotFoundException("Job " + jobId + " not found.");
        }

        job.setTitle(request.getTitle().trim());
        job.setCompany(request.getCompany().trim());
        job.setLocation(blankToNull(request.getLocation()));
        job.setJobType(request.getJobType());
        job.setDescription(blankToNull(request.getDescription()));
        job.setExpiresAt(request.getExpiresAt());
        // Editing sends a listing back to moderation instead of silently keeping
        // an approved status on changed content — except for the admin's own.
        if (editor.getRole() != Role.ADMIN) {
            job.setStatus(JobStatus.PENDING);
            job.setModeratedBy(null);
            job.setModeratedAt(null);
        }
        jobPostingRepository.save(job);

        return toResponses(List.of(job), editor.getId()).get(0);
    }

    public List<JobPostingResponse> myPostings(Long userId) {
        return toResponses(jobPostingRepository.findByPostedByIdOrderByPostedAtDesc(userId), userId);
    }

    public List<JobApplicationResponse> applicationsForJob(Long jobId, Long userId) {
        JobPosting job = findOrThrow(jobId);
        if (!isOwner(job, userId)) {
            throw new ResourceNotFoundException("Job " + jobId + " not found.");
        }
        List<JobApplication> applications = jobApplicationRepository.findByJobIdOrderByAppliedAtDesc(jobId);

        Map<Long, User> applicants = new LinkedHashMap<>();
        for (JobApplication application : applications) {
            applicants.putIfAbsent(application.getApplicant().getId(), application.getApplicant());
        }
        Map<Long, UserSummaryResponse> summaries = userSummaryService.summarizeAll(applicants.values());

        List<JobApplicationResponse> result = new ArrayList<>();
        for (JobApplication application : applications) {
            result.add(JobApplicationResponse.from(application, summaries.get(application.getApplicant().getId())));
        }
        return result;
    }

    // ------------------------------------------------------------------
    // Admin moderation
    // ------------------------------------------------------------------

    public List<JobPostingResponse> adminQueue(JobStatus status) {
        List<JobPosting> jobs = status != null
                ? jobPostingRepository.findByStatusOrderByPostedAtDesc(status)
                : jobPostingRepository.findAll();
        return toResponses(jobs, null);
    }

    @Transactional
    public JobPostingResponse adminSetStatus(Long jobId, JobStatus status, User admin) {
        JobPosting job = findOrThrow(jobId);
        job.setStatus(status);
        job.setModeratedBy(admin);
        job.setModeratedAt(Instant.now());
        jobPostingRepository.save(job);

        notificationService.notify(job.getPostedBy(), NotificationCategory.JOB,
                "Your \"" + job.getTitle() + "\" listing was " + status.name().toLowerCase() + ".");
        if (status == JobStatus.APPROVED) {
            auditLogService.record(admin, AuditAction.APPROVE_JOB, job.getTitle() + " (" + job.getCompany() + ")");
        } else if (status == JobStatus.REJECTED) {
            auditLogService.record(admin, AuditAction.REJECT_JOB, job.getTitle() + " (" + job.getCompany() + ")");
        }
        // REMOVED / EXPIRED aren't in the audit-action vocabulary, so they
        // change status without a matching log entry.

        return toResponses(List.of(job), admin.getId()).get(0);
    }

    // ------------------------------------------------------------------

    /** Approved and not past its expiry date. */
    private boolean isOpen(JobPosting job) {
        return job.getStatus() == JobStatus.APPROVED
                && (job.getExpiresAt() == null || !job.getExpiresAt().isBefore(LocalDate.now()));
    }

    private boolean isOwner(JobPosting job, Long userId) {
        return userId != null && job.getPostedBy().getId().equals(userId);
    }

    /** One summary query for all posters and one query for the viewer's applications, however long the list. */
    private List<JobPostingResponse> toResponses(List<JobPosting> jobs, Long viewerUserId) {
        if (jobs.isEmpty()) {
            return List.of();
        }
        Map<Long, User> posters = new LinkedHashMap<>();
        for (JobPosting job : jobs) {
            posters.putIfAbsent(job.getPostedBy().getId(), job.getPostedBy());
        }
        Map<Long, UserSummaryResponse> summaries = userSummaryService.summarizeAll(posters.values());
        Set<Long> appliedJobIds = appliedJobIds(viewerUserId);

        List<JobPostingResponse> result = new ArrayList<>();
        for (JobPosting job : jobs) {
            result.add(JobPostingResponse.from(job, summaries.get(job.getPostedBy().getId()),
                    viewerUserId, appliedJobIds.contains(job.getId())));
        }
        return result;
    }

    private Set<Long> appliedJobIds(Long viewerUserId) {
        if (viewerUserId == null) {
            return Set.of();
        }
        return jobApplicationRepository.findByApplicantIdOrderByAppliedAtDesc(viewerUserId).stream()
                .map(application -> application.getJob().getId())
                .collect(Collectors.toSet());
    }

    private JobPosting findOrThrow(Long id) {
        return jobPostingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job " + id + " not found."));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
