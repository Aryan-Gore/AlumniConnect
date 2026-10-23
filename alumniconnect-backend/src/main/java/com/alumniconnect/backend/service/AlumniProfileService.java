package com.alumniconnect.backend.service;

import com.alumniconnect.backend.dto.alumni.*;
import com.alumniconnect.backend.entity.*;
import com.alumniconnect.backend.exception.BadRequestException;
import com.alumniconnect.backend.exception.ResourceNotFoundException;
import com.alumniconnect.backend.repository.AlumniRecordRepository;
import com.alumniconnect.backend.repository.CareerHistoryEntryRepository;
import com.alumniconnect.backend.repository.CertificationRepository;
import com.alumniconnect.backend.repository.EducationRecordRepository;
import com.alumniconnect.backend.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Everything an alumnus does to their OWN profile: the alumni-controlled
 * fields, skills, and the four repeatable sections (work experience,
 * education, projects, certifications &amp; achievements).
 *
 * Institution-controlled fields (name, enrollment number, department, batch,
 * graduation year, verified badge) are unreachable from here by
 * construction — AlumniSelfUpdateRequest has no such fields.
 *
 * Each section item is looked up by (id, alumni id), so one alumnus can never
 * touch another's rows by guessing an id; a miss is a 404.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlumniProfileService {

    private static final int MAX_TECHNOLOGIES = 20;
    private static final int MAX_TECHNOLOGY_LENGTH = 50;

    private final AlumniRecordRepository alumniRecordRepository;
    private final CareerHistoryEntryRepository careerHistoryRepository;
    private final EducationRecordRepository educationRepository;
    private final ProjectRepository projectRepository;
    private final CertificationRepository certificationRepository;
    private final SkillService skillService;
    private final AlumniMapper alumniMapper;

    // ------------------------------------------------------------------
    // Whole profile
    // ------------------------------------------------------------------

    public AlumniResponse getOwnProfile(Long userId) {
        return alumniMapper.toDetail(requireOwnRecord(userId), Role.ALUMNI, userId);
    }

    @Transactional
    public AlumniResponse updateOwnProfile(Long userId, AlumniSelfUpdateRequest request) {
        AlumniRecord record = requireOwnRecord(userId);

        if (request.getPhone() != null) record.setPhone(blankToNull(request.getPhone()));
        if (request.getCurrentLocation() != null) record.setCurrentLocation(blankToNull(request.getCurrentLocation()));
        if (request.getCurrentCompany() != null) record.setCurrentCompany(blankToNull(request.getCurrentCompany()));
        if (request.getJobTitle() != null) record.setJobTitle(blankToNull(request.getJobTitle()));
        if (request.getIndustry() != null) record.setIndustry(blankToNull(request.getIndustry()));
        if (request.getWorkLocation() != null) record.setWorkLocation(blankToNull(request.getWorkLocation()));
        if (request.getExperienceYears() != null) record.setExperienceYears(request.getExperienceYears());
        if (request.getLinkedinUrl() != null) record.setLinkedinUrl(blankToNull(request.getLinkedinUrl()));
        if (request.getGithubUrl() != null) record.setGithubUrl(blankToNull(request.getGithubUrl()));
        if (request.getPortfolioUrl() != null) record.setPortfolioUrl(blankToNull(request.getPortfolioUrl()));
        if (request.getProfessionalSummary() != null) record.setProfessionalSummary(blankToNull(request.getProfessionalSummary()));
        if (request.getOpenToMentoring() != null) record.setOpenToMentoring(request.getOpenToMentoring());
        if (request.getOpenToNetworking() != null) record.setOpenToNetworking(request.getOpenToNetworking());
        if (request.getAvailableForHiring() != null) record.setAvailableForHiring(request.getAvailableForHiring());
        if (request.getInterestedInEvents() != null) record.setInterestedInEvents(request.getInterestedInEvents());
        if (request.getDirectoryVisibility() != null) record.setDirectoryVisibility(request.getDirectoryVisibility());
        if (request.getEmailVisibility() != null) record.setEmailVisibility(request.getEmailVisibility());
        if (request.getPhoneVisibility() != null) record.setPhoneVisibility(request.getPhoneVisibility());

        if (request.getSkills() != null) {
            Set<Skill> skills = skillService.resolve(request.getSkills());
            // Mutate the managed collection in place so Hibernate writes only the rows that changed.
            record.getSkills().clear();
            record.getSkills().addAll(skills);
        }

        alumniRecordRepository.save(record);
        return alumniMapper.toDetail(record, Role.ALUMNI, userId);
    }

    // ------------------------------------------------------------------
    // Work experience
    // ------------------------------------------------------------------

    @Transactional
    public CareerHistoryResponse addCareerHistory(Long userId, CareerHistoryRequest request) {
        AlumniRecord record = requireOwnRecord(userId);
        requireNotBefore(request.getStartDate(), request.getEndDate(), "End date can't be before the start date.");

        CareerHistoryEntry entry = careerHistoryRepository.save(CareerHistoryEntry.builder()
                .alumniRecord(record)
                .company(request.getCompany().trim())
                .jobTitle(request.getJobTitle().trim())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .description(blankToNull(request.getDescription()))
                .build());
        return CareerHistoryResponse.from(entry);
    }

    @Transactional
    public CareerHistoryResponse updateCareerHistory(Long userId, Long entryId, CareerHistoryRequest request) {
        AlumniRecord record = requireOwnRecord(userId);
        requireNotBefore(request.getStartDate(), request.getEndDate(), "End date can't be before the start date.");

        CareerHistoryEntry entry = careerHistoryRepository.findByIdAndAlumniRecordId(entryId, record.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Work experience entry not found."));
        entry.setCompany(request.getCompany().trim());
        entry.setJobTitle(request.getJobTitle().trim());
        entry.setStartDate(request.getStartDate());
        entry.setEndDate(request.getEndDate());
        entry.setDescription(blankToNull(request.getDescription()));
        return CareerHistoryResponse.from(careerHistoryRepository.save(entry));
    }

    @Transactional
    public void deleteCareerHistory(Long userId, Long entryId) {
        AlumniRecord record = requireOwnRecord(userId);
        CareerHistoryEntry entry = careerHistoryRepository.findByIdAndAlumniRecordId(entryId, record.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Work experience entry not found."));
        careerHistoryRepository.delete(entry);
    }

    // ------------------------------------------------------------------
    // Education (additional to the degree the institution certifies)
    // ------------------------------------------------------------------

    @Transactional
    public EducationResponse addEducation(Long userId, EducationRequest request) {
        AlumniRecord record = requireOwnRecord(userId);
        requireYearsInOrder(request);

        EducationRecord education = educationRepository.save(EducationRecord.builder()
                .alumniRecord(record)
                .degree(request.getDegree().trim())
                .program(blankToNull(request.getProgram()))
                .institutionName(request.getInstitutionName().trim())
                .startYear(request.getStartYear())
                .endYear(request.getEndYear())
                .cgpa(request.getCgpa())
                .build());
        return EducationResponse.from(education);
    }

    @Transactional
    public EducationResponse updateEducation(Long userId, Long educationId, EducationRequest request) {
        AlumniRecord record = requireOwnRecord(userId);
        requireYearsInOrder(request);

        EducationRecord education = educationRepository.findByIdAndAlumniRecordId(educationId, record.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Education entry not found."));
        education.setDegree(request.getDegree().trim());
        education.setProgram(blankToNull(request.getProgram()));
        education.setInstitutionName(request.getInstitutionName().trim());
        education.setStartYear(request.getStartYear());
        education.setEndYear(request.getEndYear());
        education.setCgpa(request.getCgpa());
        return EducationResponse.from(educationRepository.save(education));
    }

    @Transactional
    public void deleteEducation(Long userId, Long educationId) {
        AlumniRecord record = requireOwnRecord(userId);
        EducationRecord education = educationRepository.findByIdAndAlumniRecordId(educationId, record.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Education entry not found."));
        educationRepository.delete(education);
    }

    // ------------------------------------------------------------------
    // Projects
    // ------------------------------------------------------------------

    @Transactional
    public ProjectResponse addProject(Long userId, ProjectRequest request) {
        AlumniRecord record = requireOwnRecord(userId);
        requireNotBefore(request.getStartDate(), request.getEndDate(), "End date can't be before the start date.");

        Project project = projectRepository.save(Project.builder()
                .alumniRecord(record)
                .name(request.getName().trim())
                .description(blankToNull(request.getDescription()))
                .technologies(cleanTechnologies(request.getTechnologies()))
                .projectUrl(blankToNull(request.getProjectUrl()))
                .githubUrl(blankToNull(request.getGithubUrl()))
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build());
        return ProjectResponse.from(project);
    }

    @Transactional
    public ProjectResponse updateProject(Long userId, Long projectId, ProjectRequest request) {
        AlumniRecord record = requireOwnRecord(userId);
        requireNotBefore(request.getStartDate(), request.getEndDate(), "End date can't be before the start date.");

        Project project = projectRepository.findByIdAndAlumniRecordId(projectId, record.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found."));
        project.setName(request.getName().trim());
        project.setDescription(blankToNull(request.getDescription()));
        project.setTechnologies(cleanTechnologies(request.getTechnologies()));
        project.setProjectUrl(blankToNull(request.getProjectUrl()));
        project.setGithubUrl(blankToNull(request.getGithubUrl()));
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        return ProjectResponse.from(projectRepository.save(project));
    }

    @Transactional
    public void deleteProject(Long userId, Long projectId) {
        AlumniRecord record = requireOwnRecord(userId);
        Project project = projectRepository.findByIdAndAlumniRecordId(projectId, record.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found."));
        projectRepository.delete(project);
    }

    // ------------------------------------------------------------------
    // Certifications, achievements, awards, publications
    // ------------------------------------------------------------------

    @Transactional
    public CertificationResponse addCertification(Long userId, CertificationRequest request) {
        AlumniRecord record = requireOwnRecord(userId);

        Certification certification = certificationRepository.save(Certification.builder()
                .alumniRecord(record)
                .type(request.getType())
                .title(request.getTitle().trim())
                .issuer(blankToNull(request.getIssuer()))
                .issueDate(request.getIssueDate())
                .credentialUrl(blankToNull(request.getCredentialUrl()))
                .build());
        return CertificationResponse.from(certification);
    }

    @Transactional
    public CertificationResponse updateCertification(Long userId, Long certificationId, CertificationRequest request) {
        AlumniRecord record = requireOwnRecord(userId);
        Certification certification = certificationRepository.findByIdAndAlumniRecordId(certificationId, record.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Certification not found."));

        certification.setType(request.getType());
        certification.setTitle(request.getTitle().trim());
        certification.setIssuer(blankToNull(request.getIssuer()));
        certification.setIssueDate(request.getIssueDate());
        certification.setCredentialUrl(blankToNull(request.getCredentialUrl()));
        return CertificationResponse.from(certificationRepository.save(certification));
    }

    @Transactional
    public void deleteCertification(Long userId, Long certificationId) {
        AlumniRecord record = requireOwnRecord(userId);
        Certification certification = certificationRepository.findByIdAndAlumniRecordId(certificationId, record.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Certification not found."));
        certificationRepository.delete(certification);
    }

    // ------------------------------------------------------------------

    private AlumniRecord requireOwnRecord(Long userId) {
        return alumniRecordRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No alumni record is linked to this account."));
    }

    private void requireNotBefore(LocalDate start, LocalDate end, String message) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new BadRequestException(message);
        }
    }

    private void requireYearsInOrder(EducationRequest request) {
        if (request.getStartYear() != null && request.getEndYear() != null
                && request.getEndYear() < request.getStartYear()) {
            throw new BadRequestException("End year can't be before the start year.");
        }
    }

    /** Trims, drops blanks and case-duplicates, and enforces the per-entry and total limits. */
    private List<String> cleanTechnologies(List<String> raw) {
        if (raw == null) return new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        List<String> cleaned = new ArrayList<>();
        for (String technology : raw) {
            if (technology == null || technology.isBlank()) continue;
            String name = technology.trim();
            if (name.length() > MAX_TECHNOLOGY_LENGTH) {
                throw new BadRequestException("A technology name can be at most " + MAX_TECHNOLOGY_LENGTH + " characters.");
            }
            if (seen.add(name.toLowerCase())) cleaned.add(name);
        }
        if (cleaned.size() > MAX_TECHNOLOGIES) {
            throw new BadRequestException("List at most " + MAX_TECHNOLOGIES + " technologies per project.");
        }
        return cleaned;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
