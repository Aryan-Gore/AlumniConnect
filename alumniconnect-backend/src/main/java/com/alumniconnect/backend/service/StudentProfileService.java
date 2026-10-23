package com.alumniconnect.backend.service;

import com.alumniconnect.backend.dto.common.DepartmentRef;
import com.alumniconnect.backend.dto.student.StudentProfileResponse;
import com.alumniconnect.backend.dto.student.StudentProfileUpdateRequest;
import com.alumniconnect.backend.entity.StudentProfile;
import com.alumniconnect.backend.entity.User;
import com.alumniconnect.backend.exception.ResourceNotFoundException;
import com.alumniconnect.backend.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentProfileService {

    private final StudentProfileRepository studentProfileRepository;
    private final DepartmentService departmentService;
    private final ProfileCompletionCalculator completionCalculator;

    public StudentProfileResponse getOwnProfile(Long userId) {
        return toResponse(requireProfile(userId));
    }

    @Transactional
    public StudentProfileResponse updateOwnProfile(Long userId, StudentProfileUpdateRequest request) {
        StudentProfile profile = requireProfile(userId);

        if (request.getBatch() != null) profile.setBatch(blankToNull(request.getBatch()));
        if (request.getCareerInterests() != null) profile.setCareerInterests(blankToNull(request.getCareerInterests()));
        if (request.getSkills() != null) profile.setSkills(cleanSkills(request.getSkills()));
        if (request.getDepartmentId() != null) {
            profile.setDepartment(departmentService.require(request.getDepartmentId()));
        }

        studentProfileRepository.save(profile);
        return toResponse(profile);
    }

    private StudentProfileResponse toResponse(StudentProfile profile) {
        User user = profile.getUser();
        return StudentProfileResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .enrollmentNumber(profile.getEnrollmentNumber())
                .department(DepartmentRef.from(profile.getDepartment()))
                .batch(profile.getBatch())
                .skills(profile.getSkills() == null ? List.of() : List.copyOf(profile.getSkills()))
                .careerInterests(profile.getCareerInterests())
                .completion(completionCalculator.forStudent(profile))
                .build();
    }

    private StudentProfile requireProfile(Long userId) {
        return studentProfileRepository.findWithDepartmentByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No student profile is linked to this account."));
    }

    /** Student skills are a plain list (no shared Skill rows): trim, drop blanks and case-duplicates. */
    private List<String> cleanSkills(List<String> raw) {
        Set<String> seen = new HashSet<>();
        List<String> cleaned = new ArrayList<>();
        for (String skill : raw) {
            String name = SkillService.normalize(skill);
            if (name != null && seen.add(name.toLowerCase())) cleaned.add(name);
        }
        return cleaned;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
