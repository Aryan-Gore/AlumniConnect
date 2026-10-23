package com.alumniconnect.backend.controller;

import com.alumniconnect.backend.security.CustomUserDetails;
import com.alumniconnect.backend.service.AuditLogService;
import com.alumniconnect.backend.service.chat.ChatService;
import com.alumniconnect.backend.entity.AuditAction;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin – Chat")
@RestController
@RequestMapping("/admin/chat")
@RequiredArgsConstructor
public class AdminChatController {

    private final ChatService chatService;
    private final AuditLogService auditLogService;

    /**
     * DELETE /admin/chat/messages/{messageId}
     * Remove a message from a department room. Direct messages are not modifiable by admins.
     * Caller: ADMIN (enforced by SecurityConfig + service-layer check).
     */
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long messageId) {
        chatService.adminDeleteMessage(principal.getUser(), messageId);
        auditLogService.record(principal.getUser(), AuditAction.REMOVE_MESSAGE,
                "Removed message id=" + messageId);
        return ResponseEntity.noContent().build();
    }
}
