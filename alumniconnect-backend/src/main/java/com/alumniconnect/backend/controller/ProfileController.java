package com.alumniconnect.backend.controller;

import com.alumniconnect.backend.dto.common.UploadResponse;
import com.alumniconnect.backend.security.CustomUserDetails;
import com.alumniconnect.backend.service.ProfilePhotoService;
import com.alumniconnect.backend.service.SkillService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Profile Photo & Skills", description = "Avatar upload (any role) and skill autocomplete")
public class ProfileController {

    private final ProfilePhotoService profilePhotoService;
    private final SkillService skillService;

    @PostMapping(value = "/profile/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadPhoto(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(profilePhotoService.upload(principal.getId(), file));
    }

    @DeleteMapping("/profile/photo")
    public ResponseEntity<Void> removePhoto(@AuthenticationPrincipal CustomUserDetails principal) {
        profilePhotoService.remove(principal.getId());
        return ResponseEntity.noContent().build();
    }

    /** Autocomplete for the skills input: up to 10 existing skill names starting with the text. */
    @GetMapping("/skills/suggest")
    public ResponseEntity<List<String>> suggestSkills(@RequestParam("q") String q) {
        return ResponseEntity.ok(skillService.suggest(q));
    }
}
