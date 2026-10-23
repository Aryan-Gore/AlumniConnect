package com.alumniconnect.backend.controller;

import com.alumniconnect.backend.dto.common.UserSummaryResponse;
import com.alumniconnect.backend.dto.group.GroupDetailResponse;
import com.alumniconnect.backend.dto.group.InstituteGroups;
import com.alumniconnect.backend.entity.User;
import com.alumniconnect.backend.security.CurrentUser;
import com.alumniconnect.backend.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Department group endpoints.
 *
 * GET /groups                          — all groups grouped by institute
 * GET /groups/{departmentId}           — group detail + conversationId
 * GET /groups/{departmentId}/members   — paginated member list with optional name search
 */
@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @GetMapping
    public ResponseEntity<List<InstituteGroups>> listGroups(@CurrentUser User caller) {
        return ResponseEntity.ok(groupService.listGroups(caller));
    }

    @GetMapping("/{departmentId}")
    public ResponseEntity<GroupDetailResponse> getGroupDetail(
            @CurrentUser User caller,
            @PathVariable Long departmentId) {
        return ResponseEntity.ok(groupService.getGroupDetail(caller, departmentId));
    }

    @GetMapping("/{departmentId}/members")
    public ResponseEntity<Page<UserSummaryResponse>> getGroupMembers(
            @CurrentUser User caller,
            @PathVariable Long departmentId,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(groupService.getGroupMembers(caller, departmentId, query, page, size));
    }
}
