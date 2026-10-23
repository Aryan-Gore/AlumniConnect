package com.alumniconnect.backend.dto.alumni;

import com.alumniconnect.backend.dto.common.DepartmentRef;
import com.alumniconnect.backend.dto.common.ProfileCompletionResponse;
import com.alumniconnect.backend.entity.AlumniStatus;
import com.alumniconnect.backend.entity.ContactVisibility;
import com.alumniconnect.backend.entity.DirectoryVisibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * The full profile page. Built only by AlumniMapper, which applies the
 * viewer-dependent rules: email and phone obey the alumnus' own visibility
 * choice (null when hidden from this viewer), the enrollment number is shown
 * to the owner and admins only, and {@code completion} is for the owner and
 * admins only.
 */
@Getter
@Builder
@AllArgsConstructor
public class AlumniResponse {

    private Long id;
    /** The linked login account; null until the record is claimed. Needed to start a chat or open the feed profile. */
    private Long userId;
    private String fullName;
    private String profilePhotoUrl;
    private boolean verified;
    private AlumniStatus status;
    /** "Software Engineer at Acme" — computed, never stored. */
    private String headline;

    // ---- Institution-controlled ----
    private String enrollmentNumber;
    private String degree;
    private DepartmentRef department;
    private String batch;
    private Integer graduationYear;

    // ---- Professional ----
    private String currentCompany;
    private String jobTitle;
    private String industry;
    private String workLocation;
    private String currentLocation;
    private Integer experienceYears;
    private String professionalSummary;
    private List<String> skills;

    // ---- Contact ----
    private String email;
    private String phone;
    private String linkedinUrl;
    private String githubUrl;
    private String portfolioUrl;
    /** Only populated for the owner and admins, so the editor can show the current choice. */
    private ContactVisibility emailVisibility;
    private ContactVisibility phoneVisibility;

    // ---- Participation ----
    private boolean openToMentoring;
    private boolean openToNetworking;
    private boolean availableForHiring;
    private boolean interestedInEvents;
    private DirectoryVisibility directoryVisibility;

    // ---- Sections ----
    private List<CareerHistoryResponse> careerHistory;
    private List<EducationResponse> education;
    private List<ProjectResponse> projects;
    private List<CertificationResponse> certifications;

    // ---- Viewer-specific ----
    /** True when the viewer is looking at their own profile. */
    private boolean ownProfile;
    private ProfileCompletionResponse completion;
}
