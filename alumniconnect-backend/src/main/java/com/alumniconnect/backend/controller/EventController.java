package com.alumniconnect.backend.controller;

import com.alumniconnect.backend.dto.event.EventResponse;
import com.alumniconnect.backend.security.CustomUserDetails;
import com.alumniconnect.backend.service.EventService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Browse published events and manage your own registrations")
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventResponse>> list(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(eventService.listPublished(principal.getId()));
    }

    @GetMapping("/me")
    public ResponseEntity<List<EventResponse>> myRegistrations(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(eventService.myRegistrations(principal.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getById(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(eventService.getById(id, principal.getId()));
    }

    @PostMapping("/{id}/register")
    public ResponseEntity<EventResponse> register(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(eventService.register(id, principal.getUser()));
    }

    @DeleteMapping("/{id}/register")
    public ResponseEntity<EventResponse> cancelRegistration(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(eventService.cancelRegistration(id, principal.getId()));
    }
}
