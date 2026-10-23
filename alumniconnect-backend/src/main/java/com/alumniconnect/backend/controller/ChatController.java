package com.alumniconnect.backend.controller;

import com.alumniconnect.backend.dto.chat.ConversationResponse;
import com.alumniconnect.backend.dto.chat.MarkReadRequest;
import com.alumniconnect.backend.dto.chat.MessageResponse;
import com.alumniconnect.backend.dto.chat.ReadReceiptResponse;
import com.alumniconnect.backend.dto.chat.StartDirectRequest;
import com.alumniconnect.backend.dto.chat.UnreadCountResponse;
import com.alumniconnect.backend.dto.common.CursorPage;
import com.alumniconnect.backend.dto.common.UserSummaryResponse;
import com.alumniconnect.backend.security.CustomUserDetails;
import com.alumniconnect.backend.service.chat.ChatService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Chat")
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /** GET /chat/conversations — list all conversations for the caller, inbox-sorted. */
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationResponse>> getConversations(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(chatService.getConversations(principal.getUser()));
    }

    /**
     * POST /chat/conversations/direct — find or start a DIRECT conversation.
     * Body: { "userId": 42 }
     */
    @PostMapping("/conversations/direct")
    public ResponseEntity<ConversationResponse> startDirect(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody StartDirectRequest req) {
        return ResponseEntity.ok(chatService.startDirect(principal.getUser(), req));
    }

    /**
     * GET /chat/conversations/{conversationId}/messages — paginated messages, newest first.
     * ?before=<messageId>&size=<1-50>
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<CursorPage<MessageResponse>> getMessages(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long conversationId,
            @RequestParam(required = false) Long before,
            @RequestParam(defaultValue = "30") int size) {
        int clampedSize = Math.min(Math.max(size, 1), 50);
        return ResponseEntity.ok(chatService.getMessages(principal.getUser(), conversationId, before, clampedSize));
    }

    /**
     * POST /chat/conversations/{conversationId}/read — mark messages read.
     * Body: { "conversationId": 12, "messageId": 456 } (messageId is optional)
     */
    @PostMapping("/conversations/{conversationId}/read")
    public ResponseEntity<ReadReceiptResponse> markRead(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long conversationId,
            @Valid @RequestBody MarkReadRequest req) {
        return ResponseEntity.ok(chatService.markRead(principal.getUser(), conversationId, req.getMessageId()));
    }

    /** GET /chat/unread-count — total unread message count across all conversations. */
    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(chatService.getUnreadCount(principal.getUser()));
    }

    /** GET /chat/users/search?q=text — search users to start a DM with. */
    @GetMapping("/users/search")
    public ResponseEntity<List<UserSummaryResponse>> searchUsers(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(defaultValue = "") String q) {
        return ResponseEntity.ok(chatService.searchUsers(principal.getUser(), q));
    }
}
