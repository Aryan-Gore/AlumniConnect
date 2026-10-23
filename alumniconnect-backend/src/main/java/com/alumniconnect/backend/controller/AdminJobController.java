package com.alumniconnect.backend.controller;

import com.alumniconnect.backend.dto.job.JobPostingResponse;
import com.alumniconnect.backend.dto.job.JobStatusUpdateRequest;
import com.alumniconnect.backend.entity.JobStatus;
import com.alumniconnect.backend.security.CustomUserDetails;
import com.alumniconnect.backend.service.JobService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/jobs")
@RequiredArgsConstructor
@Tag(name = "Admin — Job Moderation", description = "Approve/reject/remove alumni-posted listings")
public class AdminJobController {

    private final JobService jobService;

    @GetMapping
    public ResponseEntity<List<JobPostingResponse>> queue(@RequestParam(required = false) JobStatus status) {
        return ResponseEntity.ok(jobService.adminQueue(status));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<JobPostingResponse> setStatus(
            @PathVariable Long id,
            @Valid @RequestBody JobStatusUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(jobService.adminSetStatus(id, request.getStatus(), principal.getUser()));
    }
}
