package com.alumniconnect.backend.dto.student;

import com.alumniconnect.backend.dto.common.DepartmentRef;
import com.alumniconnect.backend.dto.common.ProfileCompletionResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/** A student's own profile (students only ever see their own; alumni cards are the public face of the directory). */
@Getter
@Builder
@AllArgsConstructor
public class StudentProfileResponse {
    private Long userId;
    private String fullName;
    private String email;
    private String profilePhotoUrl;
    private String enrollmentNumber;
    private DepartmentRef department;
    private String batch;
    private List<String> skills;
    private String careerInterests;
    private ProfileCompletionResponse completion;
}
