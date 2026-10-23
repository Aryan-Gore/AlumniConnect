package com.alumniconnect.backend.controller;

import com.alumniconnect.backend.dto.alumni.*;
import com.alumniconnect.backend.security.CustomUserDetails;
import com.alumniconnect.backend.service.AlumniImportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/admin/alumni/import")
@RequiredArgsConstructor
@Tag(name = "Admin — Alumni Import", description = "CSV/Excel bulk import: upload -> map columns -> preview -> confirm")
public class AdminAlumniImportController {

    private final AlumniImportService importService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<ImportUploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(importService.upload(file, principal.getUser()));
    }

    @PostMapping("/{importId}/mapping")
    public ResponseEntity<Void> submitMapping(@PathVariable Long importId, @Valid @RequestBody ColumnMappingRequest request) {
        importService.submitMapping(importId, request.getMapping());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{importId}/preview")
    public ResponseEntity<ImportPreviewResponse> preview(@PathVariable Long importId) {
        return ResponseEntity.ok(importService.preview(importId));
    }

    @PostMapping("/{importId}/confirm")
    public ResponseEntity<ImportBatchSummaryResponse> confirm(@PathVariable Long importId, @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(importService.confirm(importId, principal.getUser()));
    }

    @GetMapping("/history")
    public ResponseEntity<List<ImportBatchSummaryResponse>> history() {
        return ResponseEntity.ok(importService.history());
    }
}
