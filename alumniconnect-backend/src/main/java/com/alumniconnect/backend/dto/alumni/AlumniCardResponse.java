package com.alumniconnect.backend.dto.alumni;

import com.alumniconnect.backend.dto.common.DepartmentRef;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/** One row of the alumni directory: enough for a card, no contact details, no sections. */
@Getter
@Builder
@AllArgsConstructor
public class AlumniCardResponse {
    private Long id;
    private Long userId;
    private String fullName;
    private String profilePhotoUrl;
    private boolean verified;
    private String headline;
    private DepartmentRef department;
    private String batch;
    private Integer graduationYear;
    private String currentCompany;
    private String jobTitle;
    private String currentLocation;
    private String industry;
    /** The first few skills only; the profile page has the rest. */
    private List<String> skills;
    private boolean openToMentoring;
    private boolean openToNetworking;
    private boolean availableForHiring;
}
