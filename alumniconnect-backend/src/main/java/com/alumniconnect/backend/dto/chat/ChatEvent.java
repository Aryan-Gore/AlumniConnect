package com.alumniconnect.backend.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** What the server pushes on /user/queue/messages: MESSAGE, READ or DELETED. */
@Getter
@AllArgsConstructor
public class ChatEvent {
    public static final String MESSAGE = "MESSAGE";
    public static final String READ = "READ";
    public static final String DELETED = "DELETED";

    private String type;
    private Long conversationId;
    private MessageResponse message;
    private Long messageId;

    public static ChatEvent message(MessageResponse message) {
        return new ChatEvent(MESSAGE, message.getConversationId(), message, null);
    }

    public static ChatEvent read(Long conversationId) {
        return new ChatEvent(READ, conversationId, null, null);
    }

    public static ChatEvent deleted(Long conversationId, Long messageId) {
        return new ChatEvent(DELETED, conversationId, null, messageId);
    }
}
