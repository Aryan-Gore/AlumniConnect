package com.alumniconnect.backend.dto.admin;

import com.alumniconnect.backend.entity.AuditAction;
import com.alumniconnect.backend.entity.AuditLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class AuditLogResponse {
    private Long id;
    private String adminEmail;
    private AuditAction action;
    private String details;
    private Instant timestamp;

    public static AuditLogResponse from(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .adminEmail(log.getAdmin().getEmail())
                .action(log.getAction())
                .details(log.getDetails())
                .timestamp(log.getCreatedAt())
                .build();
    }
}
