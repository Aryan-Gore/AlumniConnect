package com.alumniconnect.backend.controller;

import com.alumniconnect.backend.dto.admin.AuditLogResponse;
import com.alumniconnect.backend.entity.AuditAction;
import com.alumniconnect.backend.service.AuditLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/audit-log")
@RequiredArgsConstructor
@Tag(name = "Admin — Audit Log", description = "Read-only record of administrative actions")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> list(@RequestParam(required = false) AuditAction action) {
        return ResponseEntity.ok(auditLogService.list(action));
    }
}
