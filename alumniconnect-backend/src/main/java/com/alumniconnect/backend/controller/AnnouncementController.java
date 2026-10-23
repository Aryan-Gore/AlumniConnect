package com.alumniconnect.backend.controller;

import com.alumniconnect.backend.dto.announcement.AnnouncementResponse;
import com.alumniconnect.backend.security.CustomUserDetails;
import com.alumniconnect.backend.service.AnnouncementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/announcements")
@RequiredArgsConstructor
@Tag(name = "Announcements", description = "Role/department/batch-filtered institutional announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    public ResponseEntity<List<AnnouncementResponse>> feed(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(announcementService.feedFor(principal.getUser()));
    }
}
