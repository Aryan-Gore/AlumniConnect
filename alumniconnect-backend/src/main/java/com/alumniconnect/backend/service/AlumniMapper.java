package com.alumniconnect.backend.service;

import com.alumniconnect.backend.dto.alumni.*;
import com.alumniconnect.backend.dto.common.DepartmentRef;
import com.alumniconnect.backend.entity.AlumniRecord;
import com.alumniconnect.backend.entity.ContactVisibility;
import com.alumniconnect.backend.entity.Role;
import com.alumniconnect.backend.entity.Skill;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The single place where an AlumniRecord becomes JSON, and therefore the
 * single place where "who may see what" is decided:
 *
 *  - owner and admins see everything;
 *  - email / phone follow the alumnus' own PUBLIC / ALUMNI_ONLY / PRIVATE choice
 *    (ALUMNI_ONLY means viewers whose role is ALUMNI);
 *  - the enrollment number and the completion checklist never leave the owner/admin.
 *
 * Reads lazy associations, so every caller must be inside a transaction.
 */
@Component
@RequiredArgsConstructor
public class AlumniMapper {

    private static final int CARD_SKILL_LIMIT = 6;

    private final ProfileCompletionCalculator completionCalculator;

    public AlumniResponse toDetail(AlumniRecord r, Role viewerRole, Long viewerUserId) {
        boolean owner = isOwner(r, viewerUserId);
        boolean privileged = owner || viewerRole == Role.ADMIN;

        return AlumniResponse.builder()
                .id(r.getId())
                .userId(r.getUser() != null ? r.getUser().getId() : null)
                .fullName(r.getFullName())
                .profilePhotoUrl(r.getUser() != null ? r.getUser().getProfilePhotoUrl() : null)
                .verified(r.isVerified())
                .status(r.getStatus())
                .headline(headlineOf(r))
                .enrollmentNumber(privileged ? r.getEnrollmentNumber() : null)
                .degree(r.getDegree())
                .department(DepartmentRef.from(r.getDepartment()))
                .batch(r.getBatch())
                .graduationYear(r.getGraduationYear())
                .currentCompany(r.getCurrentCompany())
                .jobTitle(r.getJobTitle())
                .industry(r.getIndustry())
                .workLocation(r.getWorkLocation())
                .currentLocation(r.getCurrentLocation())
                .experienceYears(r.getExperienceYears())
                .professionalSummary(r.getProfessionalSummary())
                .skills(skillNames(r))
                .email(canSee(r.getEmailVisibility(), privileged, viewerRole) ? r.getEmail() : null)
                .phone(canSee(r.getPhoneVisibility(), privileged, viewerRole) ? r.getPhone() : null)
                .linkedinUrl(r.getLinkedinUrl())
                .githubUrl(r.getGithubUrl())
                .portfolioUrl(r.getPortfolioUrl())
                .emailVisibility(privileged ? r.getEmailVisibility() : null)
                .phoneVisibility(privileged ? r.getPhoneVisibility() : null)
                .openToMentoring(r.isOpenToMentoring())
                .openToNetworking(r.isOpenToNetworking())
                .availableForHiring(r.isAvailableForHiring())
                .interestedInEvents(r.isInterestedInEvents())
                .directoryVisibility(r.getDirectoryVisibility())
                .careerHistory(r.getCareerHistory().stream().map(CareerHistoryResponse::from).collect(Collectors.toList()))
                .education(r.getEducationRecords().stream().map(EducationResponse::from).collect(Collectors.toList()))
                .projects(r.getProjects().stream().map(ProjectResponse::from).collect(Collectors.toList()))
                .certifications(r.getCertifications().stream().map(CertificationResponse::from).collect(Collectors.toList()))
                .ownProfile(owner)
                .completion(privileged ? completionCalculator.forAlumni(r) : null)
                .build();
    }

    public AlumniCardResponse toCard(AlumniRecord r) {
        List<String> skills = skillNames(r);
        return AlumniCardResponse.builder()
                .id(r.getId())
                .userId(r.getUser() != null ? r.getUser().getId() : null)
                .fullName(r.getFullName())
                .profilePhotoUrl(r.getUser() != null ? r.getUser().getProfilePhotoUrl() : null)
                .verified(r.isVerified())
                .headline(headlineOf(r))
                .department(DepartmentRef.from(r.getDepartment()))
                .batch(r.getBatch())
                .graduationYear(r.getGraduationYear())
                .currentCompany(r.getCurrentCompany())
                .jobTitle(r.getJobTitle())
                .currentLocation(r.getCurrentLocation())
                .industry(r.getIndustry())
                .skills(skills.size() > CARD_SKILL_LIMIT ? skills.subList(0, CARD_SKILL_LIMIT) : skills)
                .openToMentoring(r.isOpenToMentoring())
                .openToNetworking(r.isOpenToNetworking())
                .availableForHiring(r.isAvailableForHiring())
                .build();
    }

    public AlumniAdminRowResponse toAdminRow(AlumniRecord r) {
        return AlumniAdminRowResponse.builder()
                .id(r.getId())
                .userId(r.getUser() != null ? r.getUser().getId() : null)
                .fullName(r.getFullName())
                .email(r.getEmail())
                .enrollmentNumber(r.getEnrollmentNumber())
                .degree(r.getDegree())
                .department(DepartmentRef.from(r.getDepartment()))
                .batch(r.getBatch())
                .graduationYear(r.getGraduationYear())
                .status(r.getStatus())
                .verified(r.isVerified())
                .currentCompany(r.getCurrentCompany())
                .jobTitle(r.getJobTitle())
                .invitedAt(r.getInvitedAt())
                .claimedAt(r.getClaimedAt())
                .build();
    }

    /** "Software Engineer at Acme" → falls back gracefully when the alumnus hasn't filled anything in yet. */
    public static String headlineOf(AlumniRecord r) {
        boolean hasTitle = notBlank(r.getJobTitle());
        boolean hasCompany = notBlank(r.getCurrentCompany());
        if (hasTitle && hasCompany) return r.getJobTitle().trim() + " at " + r.getCurrentCompany().trim();
        if (hasTitle) return r.getJobTitle().trim();
        if (hasCompany) return "Works at " + r.getCurrentCompany().trim();

        StringBuilder fallback = new StringBuilder("Alumnus");
        if (r.getDepartment() != null) {
            String dept = notBlank(r.getDepartment().getCode()) ? r.getDepartment().getCode() : r.getDepartment().getName();
            fallback.append(" · ").append(dept);
        }
        if (r.getGraduationYear() != null) {
            fallback.append(" · Class of ").append(r.getGraduationYear());
        }
        return fallback.toString();
    }

    private static boolean isOwner(AlumniRecord r, Long viewerUserId) {
        return r.getUser() != null && viewerUserId != null && r.getUser().getId().equals(viewerUserId);
    }

    private static boolean canSee(ContactVisibility visibility, boolean privileged, Role viewerRole) {
        if (privileged) return true;
        return switch (visibility) {
            case PUBLIC -> true;
            case ALUMNI_ONLY -> viewerRole == Role.ALUMNI;
            case PRIVATE -> false;
        };
    }

    private static List<String> skillNames(AlumniRecord r) {
        return r.getSkills().stream().map(Skill::getName).collect(Collectors.toList());
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}
