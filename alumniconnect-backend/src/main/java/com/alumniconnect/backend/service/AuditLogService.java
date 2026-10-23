package com.alumniconnect.backend.service;

import com.alumniconnect.backend.dto.admin.AuditLogResponse;
import com.alumniconnect.backend.entity.AuditAction;
import com.alumniconnect.backend.entity.AuditLog;
import com.alumniconnect.backend.entity.User;
import com.alumniconnect.backend.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

/**
 * Deliberately has no update/delete method — see AuditLog's javadoc. Every
 * admin-facing service call records() through here rather than touching
 * AuditLogRepository directly, so this stays the one place log entries
 * can be written from. The two list() methods below are read-only.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void record(User admin, AuditAction action, String details) {
        auditLogRepository.save(AuditLog.builder()
                .admin(admin)
                .action(action)
                .details(details)
                .build());
    }

    public List<AuditLogResponse> list(AuditAction actionFilter) {
        List<AuditLog> logs = actionFilter != null
                ? auditLogRepository.findByActionOrderByCreatedAtDesc(actionFilter)
                : auditLogRepository.findAllByOrderByCreatedAtDesc();
        return logs.stream().map(AuditLogResponse::from).collect(Collectors.toList());
    }
}
