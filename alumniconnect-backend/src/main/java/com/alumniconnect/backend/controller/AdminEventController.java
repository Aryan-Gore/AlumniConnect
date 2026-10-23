package com.alumniconnect.backend.controller;

import com.alumniconnect.backend.dto.event.EventRequest;
import com.alumniconnect.backend.dto.event.EventResponse;
import com.alumniconnect.backend.security.CustomUserDetails;
import com.alumniconnect.backend.service.EventService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Tag(name = "Admin — Events", description = "Create, publish, and cancel institutional events")
public class AdminEventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventResponse>> list() {
        return ResponseEntity.ok(eventService.adminList());
    }

    @PostMapping
    public ResponseEntity<EventResponse> create(@Valid @RequestBody EventRequest request, @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(eventService.adminCreate(request, principal.getUser()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> update(@PathVariable Long id, @Valid @RequestBody EventRequest request, @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(eventService.adminUpdate(id, request, principal.getUser()));
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<EventResponse> publish(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(eventService.adminPublish(id, principal.getUser()));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<EventResponse> cancel(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(eventService.adminCancel(id, principal.getUser()));
    }
}
