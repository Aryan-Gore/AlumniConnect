package com.alumniconnect.backend.controller;

import com.alumniconnect.backend.dto.alumni.*;
import com.alumniconnect.backend.dto.common.PageResponse;
import com.alumniconnect.backend.security.CustomUserDetails;
import com.alumniconnect.backend.service.AlumniProfileService;
import com.alumniconnect.backend.service.AlumniService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/alumni")
@RequiredArgsConstructor
@Tag(name = "Alumni Directory & Profile", description = "Search, profile detail, and the alumnus' own profile editor")
public class AlumniController {

    private final AlumniService alumniService;
    private final AlumniProfileService profileService;

    // ---- Directory (any signed-in role) ----

    @GetMapping
    public ResponseEntity<PageResponse<AlumniCardResponse>> search(
            AlumniSearchRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(alumniService.search(request, principal.getRole()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlumniResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(alumniService.getById(id, principal.getRole(), principal.getId()));
    }

    // ---- The alumnus' own profile (ROLE_ALUMNI only — see SecurityConfig) ----

    @GetMapping("/me")
    public ResponseEntity<AlumniResponse> getOwnProfile(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(profileService.getOwnProfile(principal.getId()));
    }

    @PatchMapping("/me")
    public ResponseEntity<AlumniResponse> updateOwnProfile(
            @Valid @RequestBody AlumniSelfUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(profileService.updateOwnProfile(principal.getId(), request));
    }

    // ---- Work experience ----

    @PostMapping("/me/career-history")
    public ResponseEntity<CareerHistoryResponse> addCareerHistory(
            @Valid @RequestBody CareerHistoryRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(profileService.addCareerHistory(principal.getId(), request));
    }

    @PutMapping("/me/career-history/{entryId}")
    public ResponseEntity<CareerHistoryResponse> updateCareerHistory(
            @PathVariable Long entryId,
            @Valid @RequestBody CareerHistoryRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(profileService.updateCareerHistory(principal.getId(), entryId, request));
    }

    @DeleteMapping("/me/career-history/{entryId}")
    public ResponseEntity<Void> deleteCareerHistory(
            @PathVariable Long entryId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        profileService.deleteCareerHistory(principal.getId(), entryId);
        return ResponseEntity.noContent().build();
    }

    // ---- Education ----

    @PostMapping("/me/education")
    public ResponseEntity<EducationResponse> addEducation(
            @Valid @RequestBody EducationRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(profileService.addEducation(principal.getId(), request));
    }

    @PutMapping("/me/education/{educationId}")
    public ResponseEntity<EducationResponse> updateEducation(
            @PathVariable Long educationId,
            @Valid @RequestBody EducationRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(profileService.updateEducation(principal.getId(), educationId, request));
    }

    @DeleteMapping("/me/education/{educationId}")
    public ResponseEntity<Void> deleteEducation(
            @PathVariable Long educationId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        profileService.deleteEducation(principal.getId(), educationId);
        return ResponseEntity.noContent().build();
    }

    // ---- Projects ----

    @PostMapping("/me/projects")
    public ResponseEntity<ProjectResponse> addProject(
            @Valid @RequestBody ProjectRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(profileService.addProject(principal.getId(), request));
    }

    @PutMapping("/me/projects/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(profileService.updateProject(principal.getId(), projectId, request));
    }

    @DeleteMapping("/me/projects/{projectId}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        profileService.deleteProject(principal.getId(), projectId);
        return ResponseEntity.noContent().build();
    }

    // ---- Certifications & achievements ----

    @PostMapping("/me/certifications")
    public ResponseEntity<CertificationResponse> addCertification(
            @Valid @RequestBody CertificationRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(profileService.addCertification(principal.getId(), request));
    }

    @PutMapping("/me/certifications/{certificationId}")
    public ResponseEntity<CertificationResponse> updateCertification(
            @PathVariable Long certificationId,
            @Valid @RequestBody CertificationRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(profileService.updateCertification(principal.getId(), certificationId, request));
    }

    @DeleteMapping("/me/certifications/{certificationId}")
    public ResponseEntity<Void> deleteCertification(
            @PathVariable Long certificationId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        profileService.deleteCertification(principal.getId(), certificationId);
        return ResponseEntity.noContent().build();
    }
}
