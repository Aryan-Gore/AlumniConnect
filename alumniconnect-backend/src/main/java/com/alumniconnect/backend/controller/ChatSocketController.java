package com.alumniconnect.backend.controller;

import com.alumniconnect.backend.dto.chat.ChatErrorResponse;
import com.alumniconnect.backend.dto.chat.MarkReadRequest;
import com.alumniconnect.backend.dto.chat.MessageResponse;
import com.alumniconnect.backend.dto.chat.SendMessageRequest;
import com.alumniconnect.backend.security.StompPrincipal;
import com.alumniconnect.backend.service.chat.ChatService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * Handles STOMP frames sent to /app/chat.send and /app/chat.read.
 * The principal is a StompPrincipal whose name is the user id string.
 */
@Slf4j
@Tag(name = "Chat WebSocket")
@Controller
@RequiredArgsConstructor
public class ChatSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate template;

    /**
     * Client sends: SEND /app/chat.send { conversationId, content, clientId }
     * The response is pushed to all participants via ChatPushService (not returned here).
     */
    @MessageMapping("/chat.send")
    public void send(SendMessageRequest req, Principal principal) {
        StompPrincipal sp = (StompPrincipal) principal;
        // sendMessage handles broadcast via ChatPushService inside a @Transactional method
        chatService.sendMessage(sp.getUser(), req);
    }

    /**
     * Client sends: SEND /app/chat.read { conversationId, messageId? }
     * Updates the read cursor server-side and broadcasts READ events to others.
     */
    @MessageMapping("/chat.read")
    public void markRead(MarkReadRequest req, Principal principal) {
        StompPrincipal sp = (StompPrincipal) principal;
        chatService.markRead(sp.getUser(), req.getConversationId(), req.getMessageId());
    }

    /**
     * Any exception thrown in @MessageMapping handlers is caught here and forwarded
     * to the user's /user/queue/errors destination.
     */
    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public ChatErrorResponse handleException(Exception ex, Principal principal) {
        StompPrincipal sp = (StompPrincipal) principal;
        String clientId = null; // clientId is not available at this level; the client should correlate by conversationId
        log.warn("WebSocket error for user {}: {}", sp.getName(), ex.getMessage());
        return new ChatErrorResponse(400, ex.getMessage(), clientId);
    }
}
