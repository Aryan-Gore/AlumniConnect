package com.alumniconnect.backend.service;

import com.alumniconnect.backend.dto.announcement.AnnouncementRequest;
import com.alumniconnect.backend.dto.announcement.AnnouncementResponse;
import com.alumniconnect.backend.entity.*;
import com.alumniconnect.backend.exception.BadRequestException;
import com.alumniconnect.backend.exception.ResourceNotFoundException;
import com.alumniconnect.backend.repository.AlumniRecordRepository;
import com.alumniconnect.backend.repository.AnnouncementRepository;
import com.alumniconnect.backend.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final AlumniRecordRepository alumniRecordRepository;
    private final DepartmentService departmentService;

    /** What a signed-in person sees: published announcements aimed at their role, department or batch. */
    public List<AnnouncementResponse> feedFor(User viewer) {
        if (viewer.getRole() == Role.ADMIN) {
            // The alumni office sees everything that is live, whoever it targets.
            return announcementRepository.findByStatusOrderByPublishDateDesc(AnnouncementStatus.PUBLISHED).stream()
                    .map(AnnouncementResponse::from).collect(Collectors.toList());
        }

        Long departmentId = null;
        String batch = null;
        if (viewer.getRole() == Role.STUDENT) {
            StudentProfile profile = studentProfileRepository.findByUserId(viewer.getId()).orElse(null);
            if (profile != null) {
                departmentId = profile.getDepartment().getId();
                batch = profile.getBatch();
            }
        } else if (viewer.getRole() == Role.ALUMNI) {
            AlumniRecord record = alumniRecordRepository.findByUserId(viewer.getId()).orElse(null);
            if (record != null) {
                departmentId = record.getDepartment().getId();
                batch = record.getBatch();
            }
        }

        final Long viewerDepartmentId = departmentId;
        final String viewerBatch = batch;
        return announcementRepository.findByStatusOrderByPublishDateDesc(AnnouncementStatus.PUBLISHED).stream()
                .filter(a -> matchesAudience(a, viewer.getRole(), viewerDepartmentId, viewerBatch))
                .map(AnnouncementResponse::from)
                .collect(Collectors.toList());
    }

    /** Admin management view: every announcement, drafts and archived included. */
    public List<AnnouncementResponse> adminList() {
        return announcementRepository.findAllByOrderByPublishDateDescIdDesc().stream()
                .map(AnnouncementResponse::from).collect(Collectors.toList());
    }

    private boolean matchesAudience(Announcement a, Role role, Long departmentId, String batch) {
        return switch (a.getTargetAudience()) {
            case ALL -> true;
            case STUDENTS -> role == Role.STUDENT;
            case ALUMNI -> role == Role.ALUMNI;
            case DEPARTMENT -> departmentId != null && a.getTargetDepartment() != null
                    && departmentId.equals(a.getTargetDepartment().getId());
            case BATCH -> batch != null && batch.equals(a.getTargetBatch());
        };
    }

    @Transactional
    public AnnouncementResponse create(AnnouncementRequest request, User admin) {
        Department targetDepartment = resolveTarget(request);

        Announcement announcement = announcementRepository.save(Announcement.builder()
                .title(request.getTitle().trim())
                .content(request.getContent().trim())
                .targetAudience(request.getTargetAudience())
                .targetDepartment(targetDepartment)
                .targetBatch(request.getTargetAudience() == TargetAudience.BATCH ? request.getTargetBatch().trim() : null)
                .priority(request.getPriority())
                .publishDate(request.getPublishDate() != null ? request.getPublishDate() : LocalDate.now())
                .status(AnnouncementStatus.PUBLISHED)
                .createdBy(admin)
                .build());
        return AnnouncementResponse.from(announcement);
    }

    @Transactional
    public AnnouncementResponse update(Long id, AnnouncementRequest request) {
        Announcement announcement = findOrThrow(id);
        Department targetDepartment = resolveTarget(request);

        announcement.setTitle(request.getTitle().trim());
        announcement.setContent(request.getContent().trim());
        announcement.setTargetAudience(request.getTargetAudience());
        announcement.setTargetDepartment(targetDepartment);
        announcement.setTargetBatch(request.getTargetAudience() == TargetAudience.BATCH ? request.getTargetBatch().trim() : null);
        announcement.setPriority(request.getPriority());
        if (request.getPublishDate() != null) {
            announcement.setPublishDate(request.getPublishDate());
        }
        announcementRepository.save(announcement);
        return AnnouncementResponse.from(announcement);
    }

    @Transactional
    public AnnouncementResponse setStatus(Long id, AnnouncementStatus status) {
        Announcement announcement = findOrThrow(id);
        announcement.setStatus(status);
        announcementRepository.save(announcement);
        return AnnouncementResponse.from(announcement);
    }

    /** DEPARTMENT needs a department, BATCH needs a batch; any other audience carries neither. */
    private Department resolveTarget(AnnouncementRequest request) {
        if (request.getTargetAudience() == TargetAudience.BATCH
                && (request.getTargetBatch() == null || request.getTargetBatch().isBlank())) {
            throw new BadRequestException("Enter the batch this announcement is for.");
        }
        if (request.getTargetAudience() == TargetAudience.DEPARTMENT) {
            return departmentService.require(request.getTargetDepartmentId());
        }
        return null;
    }

    private Announcement findOrThrow(Long id) {
        return announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement " + id + " not found."));
    }
}
