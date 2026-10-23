package com.alumniconnect.backend.controller;

import com.alumniconnect.backend.dto.mentorship.MentorshipDecisionRequest;
import com.alumniconnect.backend.dto.mentorship.MentorshipRequestCreate;
import com.alumniconnect.backend.dto.mentorship.MentorshipResponse;
import com.alumniconnect.backend.security.CustomUserDetails;
import com.alumniconnect.backend.service.MentorshipService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mentorship")
@RequiredArgsConstructor
@Tag(name = "Mentorship", description = "Student-initiated mentorship requests and the alumni accept/reject inbox")
public class MentorshipController {

    private final MentorshipService mentorshipService;

    @PostMapping("/requests")
    public ResponseEntity<MentorshipResponse> sendRequest(
            @Valid @RequestBody MentorshipRequestCreate request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(mentorshipService.sendRequest(principal.getUser(), request.getAlumniId(), request.getMessage()));
    }

    @GetMapping("/requests/sent")
    public ResponseEntity<List<MentorshipResponse>> mySentRequests(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(mentorshipService.myRequests(principal.getId()));
    }

    @GetMapping("/requests/received")
    public ResponseEntity<List<MentorshipResponse>> receivedRequests(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(mentorshipService.receivedRequests(principal.getId()));
    }

    @PatchMapping("/requests/{id}")
    public ResponseEntity<MentorshipResponse> respond(
            @PathVariable Long id,
            @Valid @RequestBody MentorshipDecisionRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(mentorshipService.respondToRequest(id, principal.getId(), request.getDecision()));
    }

    @PatchMapping("/requests/{id}/complete")
    public ResponseEntity<MentorshipResponse> markCompleted(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(mentorshipService.markCompleted(id, principal.getId()));
    }
}
