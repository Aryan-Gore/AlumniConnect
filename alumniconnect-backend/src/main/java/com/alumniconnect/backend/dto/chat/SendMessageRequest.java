package com.alumniconnect.backend.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Body of POST /chat/conversations/{id}/messages and of the /app/chat.send frame. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {
    /** Only used by the socket frame; the REST path carries the id in the URL. */
    private Long conversationId;
    private String content;
    private String clientId;
}
