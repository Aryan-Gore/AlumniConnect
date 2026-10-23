package com.alumniconnect.backend.service;

import com.alumniconnect.backend.dto.auth.UserResponse;
import com.alumniconnect.backend.dto.common.DepartmentRef;
import com.alumniconnect.backend.dto.common.UserSummaryResponse;
import com.alumniconnect.backend.entity.AlumniRecord;
import com.alumniconnect.backend.entity.Role;
import com.alumniconnect.backend.entity.StudentProfile;
import com.alumniconnect.backend.entity.User;
import com.alumniconnect.backend.repository.AlumniRecordRepository;
import com.alumniconnect.backend.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Resolves the "who is this person?" details (photo, headline, department,
 * verified badge) that chat, the feed and the profile screens all show next
 * to a name. The batch method answers for a whole list with at most two
 * queries, so mapping a page of 50 posts doesn't cost 50 lookups.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserSummaryService {

    private final AlumniRecordRepository alumniRecordRepository;
    private final StudentProfileRepository studentProfileRepository;

    public UserSummaryResponse summarize(User user) {
        return summarizeAll(List.of(user)).get(user.getId());
    }

    /** Keyed by user id; preserves the input order. */
    public Map<Long, UserSummaryResponse> summarizeAll(Collection<User> users) {
        List<Long> alumniUserIds = new ArrayList<>();
        List<Long> studentUserIds = new ArrayList<>();
        for (User user : users) {
            if (user.getRole() == Role.ALUMNI) alumniUserIds.add(user.getId());
            else if (user.getRole() == Role.STUDENT) studentUserIds.add(user.getId());
        }

        Map<Long, AlumniRecord> alumniByUserId = new HashMap<>();
        if (!alumniUserIds.isEmpty()) {
            for (AlumniRecord record : alumniRecordRepository.findWithDepartmentByUserIdIn(alumniUserIds)) {
                alumniByUserId.put(record.getUser().getId(), record);
            }
        }
        Map<Long, StudentProfile> studentByUserId = new HashMap<>();
        if (!studentUserIds.isEmpty()) {
            for (StudentProfile profile : studentProfileRepository.findWithDepartmentByUserIdIn(studentUserIds)) {
                studentByUserId.put(profile.getUser().getId(), profile);
            }
        }

        Map<Long, UserSummaryResponse> result = new LinkedHashMap<>();
        for (User user : users) {
            result.put(user.getId(), build(user, alumniByUserId.get(user.getId()), studentByUserId.get(user.getId())));
        }
        return result;
    }

    /**
     * user id -> department id, for the students and claimed alumni among {@code userIds}.
     * Admins (who belong to no department) and unknown ids are simply absent. Chat rooms,
     * department-only posts and the groups pages all ask the same question, so it lives here.
     */
    public Map<Long, Long> departmentIdsOf(Collection<Long> userIds) {
        Map<Long, Long> result = new HashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return result;
        }
        for (Object[] row : studentProfileRepository.findDepartmentIdsByUserIdIn(userIds)) {
            result.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }
        for (Object[] row : alumniRecordRepository.findDepartmentIdsByUserIdIn(userIds)) {
            result.putIfAbsent(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }
        return result;
    }

    public Optional<Long> departmentIdOf(Long userId) {
        return Optional.ofNullable(departmentIdsOf(List.of(userId)).get(userId));
    }

    /** The auth-context view of a user: summary fields plus their email. */
    public UserResponse currentUser(User user) {
        UserSummaryResponse summary = summarize(user);
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .department(summary.getDepartment())
                .alumniId(summary.getAlumniId())
                .verified(summary.isVerified())
                .build();
    }

    private UserSummaryResponse build(User user, AlumniRecord alumni, StudentProfile student) {
        String headline;
        DepartmentRef department = null;
        Long alumniId = null;
        boolean verified = false;

        if (alumni != null) {
            headline = AlumniMapper.headlineOf(alumni);
            department = DepartmentRef.from(alumni.getDepartment());
            alumniId = alumni.getId();
            verified = alumni.isVerified();
        } else if (student != null) {
            department = DepartmentRef.from(student.getDepartment());
            headline = studentHeadline(student);
        } else if (user.getRole() == Role.ADMIN) {
            headline = "Alumni office";
        } else {
            headline = user.getRole() == Role.ALUMNI ? "Alumnus" : "Student";
        }

        return UserSummaryResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .role(user.getRole())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .headline(headline)
                .department(department)
                .alumniId(alumniId)
                .verified(verified)
                .build();
    }

    private String studentHeadline(StudentProfile student) {
        StringBuilder headline = new StringBuilder("Student");
        if (student.getDepartment() != null) {
            String code = student.getDepartment().getCode();
            headline.append(" · ").append(code != null && !code.isBlank() ? code : student.getDepartment().getName());
        }
        if (student.getBatch() != null && !student.getBatch().isBlank()) {
            headline.append(" · Batch ").append(student.getBatch());
        }
        return headline.toString();
    }
}
