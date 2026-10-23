package com.alumniconnect.backend.service;

import com.alumniconnect.backend.dto.alumni.*;
import com.alumniconnect.backend.dto.common.PageResponse;
import com.alumniconnect.backend.entity.*;
import com.alumniconnect.backend.exception.BadRequestException;
import com.alumniconnect.backend.exception.DuplicateResourceException;
import com.alumniconnect.backend.exception.ResourceNotFoundException;
import com.alumniconnect.backend.repository.AlumniRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static com.alumniconnect.backend.service.AlumniSpecifications.*;

/**
 * The alumni directory (what students and alumni browse) and the admin-side
 * record management. Self-service editing of one's own profile lives in
 * AlumniProfileService.
 *
 * Class-level readOnly transaction: mapping an entity to JSON walks lazy
 * associations (department, skills, sections), and with open-in-view disabled
 * that only works inside a transaction. Every method that writes overrides it.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlumniService {

    private static final int DEFAULT_PAGE_SIZE = 12;
    private static final int MAX_PAGE_SIZE = 50;
    private static final int ADMIN_PAGE_SIZE = 25;

    private final AlumniRecordRepository alumniRecordRepository;
    private final DepartmentService departmentService;
    private final AlumniMapper alumniMapper;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    // ------------------------------------------------------------------
    // Directory (students, alumni and admins can all reach these — what
    // differs is the visibility specification and the mapper's privacy rules)
    // ------------------------------------------------------------------

    public PageResponse<AlumniCardResponse> search(AlumniSearchRequest request, Role viewerRole) {
        Specification<AlumniRecord> spec = Specification
                .where(hasStatusIn(List.of(AlumniStatus.CLAIMED, AlumniStatus.ACTIVE)))
                .and(visibleTo(viewerRole))
                .and(keywordMatches(request.getQuery()))
                .and(hasInstituteId(request.getInstituteId()))
                .and(hasDepartmentId(request.getDepartmentId()))
                .and(hasBatch(request.getBatch()))
                .and(hasGraduationYear(request.getGraduationYear()))
                .and(hasCompany(request.getCompany()))
                .and(hasIndustry(request.getIndustry()))
                .and(hasLocation(request.getLocation()))
                .and(isOpenToMentoring(request.getOpenToMentoring()))
                .and(isAvailableForHiring(request.getAvailableForHiring()));

        if (request.getSkills() != null) {
            for (String skill : request.getSkills()) {
                spec = spec.and(hasSkill(skill));
            }
        }

        int size = clamp(request.getSize() == null ? DEFAULT_PAGE_SIZE : request.getSize(), 1, MAX_PAGE_SIZE);
        int page = Math.max(0, request.getPage() == null ? 0 : request.getPage());
        Page<AlumniRecord> result = alumniRecordRepository.findAll(spec, PageRequest.of(page, size, sortFor(request.getSort())));

        List<AlumniCardResponse> cards = result.getContent().stream().map(alumniMapper::toCard).collect(Collectors.toList());
        return PageResponse.of(result, cards);
    }

    public AlumniResponse getById(Long id, Role viewerRole, Long viewerUserId) {
        AlumniRecord record = findOrThrow(id);

        boolean isOwner = record.getUser() != null && record.getUser().getId().equals(viewerUserId);
        if (viewerRole != Role.ADMIN && !isOwner && !isListedFor(record, viewerRole)) {
            // 404, not 403: don't confirm that a hidden or unclaimed record exists.
            throw new ResourceNotFoundException("Alumni record not found.");
        }
        return alumniMapper.toDetail(record, viewerRole, viewerUserId);
    }

    /** The same visibility rule as the directory search, applied to a single record. */
    private boolean isListedFor(AlumniRecord record, Role viewerRole) {
        if (record.getStatus() != AlumniStatus.CLAIMED && record.getStatus() != AlumniStatus.ACTIVE) return false;
        return switch (record.getDirectoryVisibility()) {
            case PUBLIC, STUDENTS_AND_ALUMNI -> true;
            case ALUMNI_ONLY -> viewerRole == Role.ALUMNI;
            case PRIVATE -> false;
        };
    }

    private Sort sortFor(String sort) {
        if ("year".equalsIgnoreCase(sort)) return Sort.by(Sort.Direction.DESC, "graduationYear").and(Sort.by("fullName"));
        if ("recent".equalsIgnoreCase(sort)) return Sort.by(Sort.Direction.DESC, "claimedAt");
        return Sort.by("fullName");
    }

    // ------------------------------------------------------------------
    // Admin — full record management (institution-controlled fields only)
    // ------------------------------------------------------------------

    public PageResponse<AlumniAdminRowResponse> adminList(String query, Long departmentId, AlumniStatus status,
                                                          Integer pageParam, Integer sizeParam) {
        Specification<AlumniRecord> spec = Specification
                .where(keywordMatches(query))
                .and(hasDepartmentId(departmentId))
                .and(status != null ? hasStatusIn(List.of(status)) : null);

        int size = clamp(sizeParam == null ? ADMIN_PAGE_SIZE : sizeParam, 1, 100);
        int page = Math.max(0, pageParam == null ? 0 : pageParam);
        Page<AlumniRecord> result = alumniRecordRepository.findAll(spec, PageRequest.of(page, size, Sort.by("fullName")));

        List<AlumniAdminRowResponse> rows = result.getContent().stream().map(alumniMapper::toAdminRow).collect(Collectors.toList());
        return PageResponse.of(result, rows);
    }

    @Transactional
    public AlumniAdminRowResponse adminCreate(AdminAlumniCreateRequest request, User admin) {
        String enrollment = request.getEnrollmentNumber().trim();
        if (alumniRecordRepository.existsByEnrollmentNumber(enrollment)) {
            throw new DuplicateResourceException("An alumni record with that enrollment number already exists.");
        }
        if (alumniRecordRepository.existsByEmailIgnoreCase(request.getEmail().trim())) {
            throw new DuplicateResourceException("An alumni record with that email already exists.");
        }
        Department department = departmentService.require(request.getDepartmentId());

        AlumniRecord record = alumniRecordRepository.save(AlumniRecord.builder()
                .fullName(request.getFullName().trim())
                .email(request.getEmail().trim())
                .enrollmentNumber(enrollment)
                .degree(blankToNull(request.getDegree()))
                .department(department)
                .batch(blankToNull(request.getBatch()))
                .graduationYear(request.getGraduationYear())
                .status(AlumniStatus.IMPORTED)
                .importedAt(Instant.now())
                .build());

        auditLogService.record(admin, AuditAction.ADD_ALUMNI, record.getFullName() + " (" + record.getEnrollmentNumber() + ")");
        return alumniMapper.toAdminRow(record);
    }

    @Transactional
    public AlumniAdminRowResponse adminUpdate(Long id, AdminAlumniUpdateRequest request, User admin) {
        AlumniRecord record = findOrThrow(id);

        record.setFullName(request.getFullName().trim());
        record.setEmail(request.getEmail().trim());
        record.setDegree(blankToNull(request.getDegree()));
        record.setDepartment(departmentService.require(request.getDepartmentId()));
        record.setBatch(blankToNull(request.getBatch()));
        record.setGraduationYear(request.getGraduationYear());
        // Enrollment number is intentionally not editable here — it's the
        // claim-matching key; changing it would strand a claimed account.

        alumniRecordRepository.save(record);
        auditLogService.record(admin, AuditAction.UPDATE_ALUMNI, record.getFullName() + " (" + record.getEnrollmentNumber() + ")");
        return alumniMapper.toAdminRow(record);
    }

    @Transactional
    public AlumniAdminRowResponse adminSetActive(Long id, boolean isActive, User admin) {
        AlumniRecord record = findOrThrow(id);
        if (record.getStatus() == AlumniStatus.IMPORTED || record.getStatus() == AlumniStatus.INVITED) {
            throw new BadRequestException("This record hasn't been claimed yet — invite the alumnus instead.");
        }
        record.setStatus(isActive ? AlumniStatus.ACTIVE : AlumniStatus.INACTIVE);
        // An inactive alumnus can no longer sign in; reactivating restores access. This is the same
        // switch the admin "Accounts" screen flips (users.is_active), so the two never disagree.
        if (record.getUser() != null) {
            record.getUser().setActive(isActive);
        }
        alumniRecordRepository.save(record);

        if (!isActive) {
            auditLogService.record(admin, AuditAction.DEACTIVATE_ALUMNI, record.getFullName() + " (" + record.getEnrollmentNumber() + ")");
        }
        return alumniMapper.toAdminRow(record);
    }

    @Transactional
    public AlumniAdminRowResponse adminInviteToClaim(Long id, User admin) {
        AlumniRecord record = findOrThrow(id);
        if (record.getStatus() != AlumniStatus.IMPORTED) {
            throw new BadRequestException("Only freshly imported records can be invited.");
        }

        record.setStatus(AlumniStatus.INVITED);
        record.setInvitedAt(Instant.now());
        alumniRecordRepository.save(record);

        // Actually sending the email is intentionally out of scope here —
        // see the "Optional: Email service" line in the SRS tech stack.
        auditLogService.record(admin, AuditAction.INVITE_ALUMNI, record.getFullName() + " (" + record.getEnrollmentNumber() + ")");
        return alumniMapper.toAdminRow(record);
    }

    /** Grants or removes the "verified alumnus" badge. */
    @Transactional
    public AlumniAdminRowResponse adminSetVerified(Long id, boolean verified, User admin) {
        AlumniRecord record = findOrThrow(id);
        if (record.getUser() == null) {
            throw new BadRequestException("Only claimed profiles can be verified.");
        }
        if (record.isVerified() != verified) {
            record.setVerified(verified);
            alumniRecordRepository.save(record);

            auditLogService.record(admin, AuditAction.VERIFY_ALUMNI,
                    (verified ? "Verified " : "Removed verification from ") + record.getFullName()
                            + " (" + record.getEnrollmentNumber() + ")");
            if (verified) {
                notificationService.notify(record.getUser(), NotificationCategory.ACCOUNT,
                        "Your alumni profile is now verified by the alumni office.");
            }
        }
        return alumniMapper.toAdminRow(record);
    }

    /** Writes an audit entry, so it needs a read-write transaction even though it mostly reads. */
    @Transactional
    public String adminExportCsv(Long departmentId, AlumniStatus status, User admin) {
        List<AlumniRecord> records = alumniRecordRepository.findAll(
                Specification.where(hasDepartmentId(departmentId))
                        .and(status != null ? hasStatusIn(List.of(status)) : null),
                Sort.by("fullName"));

        StringBuilder csv = new StringBuilder("full_name,email,enrollment_number,institute,department,degree,batch,graduation_year,status\n");
        for (AlumniRecord r : records) {
            csv.append(String.join(",",
                    escapeCsv(r.getFullName()), escapeCsv(r.getEmail()), escapeCsv(r.getEnrollmentNumber()),
                    escapeCsv(r.getDepartment().getInstitute().getName()), escapeCsv(r.getDepartment().getName()),
                    escapeCsv(r.getDegree()), escapeCsv(r.getBatch()), String.valueOf(r.getGraduationYear()),
                    r.getStatus().name())).append("\n");
        }

        auditLogService.record(admin, AuditAction.EXPORT_ALUMNI, records.size() + " records exported"
                + (departmentId != null ? " (department id " + departmentId + ")" : ""));
        return csv.toString();
    }

    /**
     * Quotes a CSV cell and neutralises spreadsheet formulas: a cell that
     * starts with = + - @ would otherwise be executed by Excel when an admin
     * opens the export.
     */
    private String escapeCsv(String value) {
        if (value == null) return "";
        String safe = value;
        if (!safe.isEmpty() && "=+-@\t\r".indexOf(safe.charAt(0)) >= 0) {
            safe = "'" + safe;
        }
        boolean needsQuotes = safe.contains(",") || safe.contains("\"") || safe.contains("\n") || safe.contains("\r");
        return needsQuotes ? "\"" + safe.replace("\"", "\"\"") + "\"" : safe;
    }

    private AlumniRecord findOrThrow(Long id) {
        return alumniRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alumni record " + id + " not found."));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
