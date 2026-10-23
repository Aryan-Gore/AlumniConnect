package com.alumniconnect.backend.controller;

import com.alumniconnect.backend.dto.student.StudentProfileResponse;
import com.alumniconnect.backend.dto.student.StudentProfileUpdateRequest;
import com.alumniconnect.backend.security.CustomUserDetails;
import com.alumniconnect.backend.service.StudentProfileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
@Tag(name = "Student Profile", description = "A student's own profile (ROLE_STUDENT only)")
public class StudentController {

    private final StudentProfileService studentProfileService;

    @GetMapping("/me")
    public ResponseEntity<StudentProfileResponse> getOwnProfile(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(studentProfileService.getOwnProfile(principal.getId()));
    }

    @PatchMapping("/me")
    public ResponseEntity<StudentProfileResponse> updateOwnProfile(
            @Valid @RequestBody StudentProfileUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(studentProfileService.updateOwnProfile(principal.getId(), request));
    }
}
