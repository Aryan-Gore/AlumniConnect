package com.alumniconnect.backend.dto.alumni;

import com.alumniconnect.backend.entity.ContactVisibility;
import com.alumniconnect.backend.entity.DirectoryVisibility;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Deliberately contains ONLY alumni-controlled fields (see
 * database/ER_DIAGRAM.md). There is no way to reach an institution-controlled
 * column through this class, by construction — not just by a runtime check.
 *
 * PATCH semantics: a field that is omitted (null) is left unchanged; an
 * empty string clears a text field; {@code skills} replaces the whole list
 * when present (send [] to clear it).
 */
@Getter
@Setter
public class AlumniSelfUpdateRequest {

    @Size(max = 20, message = "Phone can be at most 20 characters")
    private String phone;

    @Size(max = 150, message = "Location can be at most 150 characters")
    private String currentLocation;

    @Size(max = 150, message = "Company can be at most 150 characters")
    private String currentCompany;

    @Size(max = 150, message = "Job title can be at most 150 characters")
    private String jobTitle;

    @Size(max = 100, message = "Industry can be at most 100 characters")
    private String industry;

    @Size(max = 150, message = "Work location can be at most 150 characters")
    private String workLocation;

    @Min(value = 0, message = "Experience can't be negative")
    @Max(value = 70, message = "Experience looks too high")
    private Integer experienceYears;

    @Size(max = 30, message = "List at most 30 skills")
    private List<String> skills;

    @Size(max = 255, message = "URL can be at most 255 characters")
    @Pattern(regexp = "^(|https?://\\S+)$", message = "Enter a full http(s) URL")
    private String linkedinUrl;

    @Size(max = 255, message = "URL can be at most 255 characters")
    @Pattern(regexp = "^(|https?://\\S+)$", message = "Enter a full http(s) URL")
    private String githubUrl;

    @Size(max = 255, message = "URL can be at most 255 characters")
    @Pattern(regexp = "^(|https?://\\S+)$", message = "Enter a full http(s) URL")
    private String portfolioUrl;

    @Size(max = 4000, message = "Summary can be at most 4000 characters")
    private String professionalSummary;

    private Boolean openToMentoring;
    private Boolean openToNetworking;
    private Boolean availableForHiring;
    private Boolean interestedInEvents;

    private DirectoryVisibility directoryVisibility;
    private ContactVisibility emailVisibility;
    private ContactVisibility phoneVisibility;
}
