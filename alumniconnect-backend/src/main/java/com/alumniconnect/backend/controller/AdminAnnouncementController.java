package com.alumniconnect.backend.controller;

import com.alumniconnect.backend.dto.announcement.AnnouncementRequest;
import com.alumniconnect.backend.dto.announcement.AnnouncementResponse;
import com.alumniconnect.backend.entity.AnnouncementStatus;
import com.alumniconnect.backend.security.CustomUserDetails;
import com.alumniconnect.backend.service.AnnouncementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/announcements")
@RequiredArgsConstructor
@Tag(name = "Admin — Announcements", description = "Compose and manage institutional announcements")
public class AdminAnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    public ResponseEntity<List<AnnouncementResponse>> list() {
        return ResponseEntity.ok(announcementService.adminList());
    }

    @PostMapping
    public ResponseEntity<AnnouncementResponse> create(
            @Valid @RequestBody AnnouncementRequest request, @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(announcementService.create(request, principal.getUser()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnnouncementResponse> update(@PathVariable Long id, @Valid @RequestBody AnnouncementRequest request) {
        return ResponseEntity.ok(announcementService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AnnouncementResponse> setStatus(@PathVariable Long id, @RequestParam AnnouncementStatus status) {
        return ResponseEntity.ok(announcementService.setStatus(id, status));
    }
}
