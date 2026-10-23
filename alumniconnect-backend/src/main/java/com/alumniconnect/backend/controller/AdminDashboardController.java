package com.alumniconnect.backend.controller;

import com.alumniconnect.backend.dto.admin.DashboardSummaryResponse;
import com.alumniconnect.backend.dto.admin.LabelCountResponse;
import com.alumniconnect.backend.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Admin — Dashboard & Analytics", description = "Overview tiles and chart data")
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    @GetMapping("/admin/dashboard/summary")
    public ResponseEntity<DashboardSummaryResponse> summary() {
        return ResponseEntity.ok(dashboardService.summary());
    }

    @GetMapping("/admin/analytics/alumni-by-department")
    public ResponseEntity<List<LabelCountResponse>> byDepartment() {
        return ResponseEntity.ok(dashboardService.summary().getByDepartment());
    }

    @GetMapping("/admin/analytics/alumni-by-institute")
    public ResponseEntity<List<LabelCountResponse>> byInstitute() {
        return ResponseEntity.ok(dashboardService.byInstitute());
    }

    @GetMapping("/admin/analytics/alumni-by-batch")
    public ResponseEntity<List<LabelCountResponse>> byBatch() {
        return ResponseEntity.ok(dashboardService.summary().getByGraduationYear());
    }

    @GetMapping("/admin/analytics/alumni-by-industry")
    public ResponseEntity<List<LabelCountResponse>> byIndustry() {
        return ResponseEntity.ok(dashboardService.byIndustry());
    }

    @GetMapping("/admin/analytics/alumni-by-location")
    public ResponseEntity<List<LabelCountResponse>> byLocation() {
        return ResponseEntity.ok(dashboardService.byLocation());
    }
}
