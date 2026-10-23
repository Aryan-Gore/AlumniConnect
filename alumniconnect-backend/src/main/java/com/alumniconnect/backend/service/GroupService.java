package com.alumniconnect.backend.service;

import com.alumniconnect.backend.dto.common.UserSummaryResponse;
import com.alumniconnect.backend.dto.group.GroupDetailResponse;
import com.alumniconnect.backend.dto.group.GroupSummary;
import com.alumniconnect.backend.dto.group.InstituteGroups;
import com.alumniconnect.backend.dto.common.InstituteRef;
import com.alumniconnect.backend.entity.Conversation;
import com.alumniconnect.backend.entity.Department;
import com.alumniconnect.backend.entity.DirectoryVisibility;
import com.alumniconnect.backend.entity.Institute;
import com.alumniconnect.backend.entity.Role;
import com.alumniconnect.backend.entity.User;
import com.alumniconnect.backend.exception.ResourceNotFoundException;
import com.alumniconnect.backend.repository.ConversationRepository;
import com.alumniconnect.backend.repository.DepartmentRepository;
import com.alumniconnect.backend.repository.GroupQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Department group browsing and member lookup.
 * A "group" is simply a Department, augmented with member-count stats
 * and a reference to the department's STOMP room Conversation (if it exists).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupService {

    private final DepartmentRepository departmentRepository;
    private final GroupQueryRepository groupQueryRepository;
    private final ConversationRepository conversationRepository;
    private final UserSummaryService userSummaryService;

    // -----------------------------------------------------------------------
    // List all groups (grouped by institute)
    // -----------------------------------------------------------------------

    public List<InstituteGroups> listGroups(User caller) {
        List<Department> departments = departmentRepository.findAllWithInstitute();
        if (departments.isEmpty()) return List.of();

        List<Long> deptIds = departments.stream().map(Department::getId).toList();

        // Batch-load member counts
        Map<Long, Long> studentCounts = toMap(
                groupQueryRepository.studentCountsByDepartments(deptIds));
        Map<Long, Long> alumniCounts = toMap(
                groupQueryRepository.alumniCountsByDepartments(deptIds));

        // Caller's department (for "member" flag)
        Optional<Long> callerDeptId = userSummaryService.departmentIdOf(caller.getId());

        // Admins are "member" of all groups
        boolean isAdmin = caller.getRole() == Role.ADMIN;

        // Build summaries
        Map<Institute, List<GroupSummary>> byInstitute = new LinkedHashMap<>();
        for (Department dept : departments) {
            long students = studentCounts.getOrDefault(dept.getId(), 0L);
            long alumni = alumniCounts.getOrDefault(dept.getId(), 0L);
            GroupSummary summary = GroupSummary.builder()
                    .departmentId(dept.getId())
                    .name(dept.getName())
                    .code(dept.getCode())
                    .instituteId(dept.getInstitute().getId())
                    .instituteCode(dept.getInstitute().getCode())
                    .memberCount(students + alumni)
                    .studentCount(students)
                    .alumniCount(alumni)
                    .member(isAdmin || callerDeptId.map(d -> d.equals(dept.getId())).orElse(false))
                    .build();
            byInstitute.computeIfAbsent(dept.getInstitute(), k -> new ArrayList<>()).add(summary);
        }

        return byInstitute.entrySet().stream()
                .map(e -> InstituteGroups.builder()
                        .institute(InstituteRef.from(e.getKey()))
                        .groups(e.getValue())
                        .build())
                .toList();
    }

    // -----------------------------------------------------------------------
    // Group detail
    // -----------------------------------------------------------------------

    public GroupDetailResponse getGroupDetail(User caller, Long departmentId) {
        Department dept = departmentRepository.findByIdWithInstitute(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found."));

        long students = groupQueryRepository.countStudents(departmentId);
        long alumni = groupQueryRepository.countAlumni(departmentId);

        Optional<Long> callerDeptId = userSummaryService.departmentIdOf(caller.getId());
        boolean isAdmin = caller.getRole() == Role.ADMIN;
        boolean member = isAdmin || callerDeptId.map(d -> d.equals(departmentId)).orElse(false);

        // Find the department's chat room conversation id (may not exist yet)
        Long conversationId = conversationRepository
                .findByDepartmentId(departmentId)
                .map(Conversation::getId)
                .orElse(null);

        return GroupDetailResponse.builder()
                .departmentId(dept.getId())
                .name(dept.getName())
                .code(dept.getCode())
                .instituteId(dept.getInstitute().getId())
                .instituteCode(dept.getInstitute().getCode())
                .instituteName(dept.getInstitute().getName())
                .memberCount(students + alumni)
                .studentCount(students)
                .alumniCount(alumni)
                .member(member)
                .conversationId(conversationId)
                .build();
    }

    // -----------------------------------------------------------------------
    // Group members
    // -----------------------------------------------------------------------

    public Page<UserSummaryResponse> getGroupMembers(User caller, Long departmentId,
                                                      String query, int page, int size) {
        departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found."));

        List<DirectoryVisibility> visible = AlumniSpecifications.visibleStatesFor(caller.getRole());
        String q = (query != null && !query.isBlank()) ? query.trim() : "";
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(size <= 0 ? 20 : size, 50));

        Page<User> members = groupQueryRepository.findMembers(departmentId, visible, q, pageable);
        Map<Long, UserSummaryResponse> summaries = userSummaryService.summarizeAll(members.getContent());
        return members.map(u -> summaries.get(u.getId()));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Map<Long, Long> toMap(List<Object[]> rows) {
        return rows.stream().collect(Collectors.toMap(
                r -> (Long) r[0],
                r -> (Long) r[1]));
    }
}
