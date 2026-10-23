package com.alumniconnect.backend.controller;

import com.alumniconnect.backend.dto.common.PublicStatsResponse;
import com.alumniconnect.backend.service.PublicStatsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
@Tag(name = "Public", description = "Anonymous, aggregate-only data for the landing page")
public class PublicController {

    private final PublicStatsService publicStatsService;

    @GetMapping("/stats")
    public ResponseEntity<PublicStatsResponse> stats() {
        return ResponseEntity.ok(publicStatsService.stats());
    }
}
