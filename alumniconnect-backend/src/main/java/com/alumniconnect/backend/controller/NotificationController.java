package com.alumniconnect.backend.controller;

import com.alumniconnect.backend.dto.notification.NotificationResponse;
import com.alumniconnect.backend.security.CustomUserDetails;
import com.alumniconnect.backend.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Full notification history, read/unread state")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> list(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(notificationService.list(principal.getId()));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(Map.of("count", notificationService.unreadCount(principal.getId())));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markRead(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(notificationService.markRead(id, principal.getId()));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal CustomUserDetails principal) {
        notificationService.markAllRead(principal.getId());
        return ResponseEntity.ok().build();
    }
}
