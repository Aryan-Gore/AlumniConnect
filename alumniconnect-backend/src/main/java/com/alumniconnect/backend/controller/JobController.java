package com.alumniconnect.backend.controller;

import com.alumniconnect.backend.dto.job.JobApplicationResponse;
import com.alumniconnect.backend.dto.job.JobPostingRequest;
import com.alumniconnect.backend.dto.job.JobPostingResponse;
import com.alumniconnect.backend.security.CustomUserDetails;
import com.alumniconnect.backend.service.JobService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
@Tag(name = "Jobs", description = "Opportunities posted by students, alumni and the office — browse/apply/post for every signed-in user, moderated by admins")
public class JobController {

    private final JobService jobService;

    @GetMapping
    public ResponseEntity<List<JobPostingResponse>> browse(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(jobService.browse(principal.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobPostingResponse> getById(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(jobService.getById(id, principal.getUser()));
    }

    @PostMapping("/{id}/apply")
    public ResponseEntity<JobApplicationResponse> apply(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(jobService.apply(id, principal.getUser()));
    }

    @GetMapping("/applications/me")
    public ResponseEntity<List<JobApplicationResponse>> myApplications(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(jobService.myApplications(principal.getId()));
    }

    @PostMapping
    public ResponseEntity<JobPostingResponse> post(@Valid @RequestBody JobPostingRequest request, @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(jobService.post(request, principal.getUser()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobPostingResponse> update(
            @PathVariable Long id, @Valid @RequestBody JobPostingRequest request, @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(jobService.update(id, request, principal.getUser()));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<JobPostingResponse>> myPostings(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(jobService.myPostings(principal.getId()));
    }

    @GetMapping("/{id}/applications")
    public ResponseEntity<List<JobApplicationResponse>> applicationsForJob(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(jobService.applicationsForJob(id, principal.getId()));
    }
}
