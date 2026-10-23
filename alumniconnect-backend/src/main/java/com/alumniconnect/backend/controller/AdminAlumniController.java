package com.alumniconnect.backend.controller;

import com.alumniconnect.backend.dto.alumni.*;
import com.alumniconnect.backend.dto.common.PageResponse;
import com.alumniconnect.backend.entity.AlumniStatus;
import com.alumniconnect.backend.security.CustomUserDetails;
import com.alumniconnect.backend.service.AlumniService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/alumni")
@RequiredArgsConstructor
@Tag(name = "Admin — Alumni Records", description = "Manual record management; ROLE_ADMIN only")
public class AdminAlumniController {

    private final AlumniService alumniService;

    @GetMapping
    public ResponseEntity<PageResponse<AlumniAdminRowResponse>> list(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) AlumniStatus status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(alumniService.adminList(query, departmentId, status, page, size));
    }

    @PostMapping
    public ResponseEntity<AlumniAdminRowResponse> create(
            @Valid @RequestBody AdminAlumniCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(alumniService.adminCreate(request, principal.getUser()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlumniAdminRowResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AdminAlumniUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(alumniService.adminUpdate(id, request, principal.getUser()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AlumniAdminRowResponse> setStatus(
            @PathVariable Long id,
            @Valid @RequestBody AlumniStatusUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(alumniService.adminSetActive(id, request.getIsActive(), principal.getUser()));
    }

    @PatchMapping("/{id}/verify")
    public ResponseEntity<AlumniAdminRowResponse> setVerified(
            @PathVariable Long id,
            @Valid @RequestBody AlumniVerifyRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(alumniService.adminSetVerified(id, request.getVerified(), principal.getUser()));
    }

    @PostMapping("/{id}/invite")
    public ResponseEntity<AlumniAdminRowResponse> invite(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(alumniService.adminInviteToClaim(id, principal.getUser()));
    }

    @GetMapping("/export")
    public ResponseEntity<String> export(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) AlumniStatus status,
            @AuthenticationPrincipal CustomUserDetails principal) {
        String csv = alumniService.adminExportCsv(departmentId, status, principal.getUser());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"alumni-export.csv\"")
                .body(csv);
    }
}
