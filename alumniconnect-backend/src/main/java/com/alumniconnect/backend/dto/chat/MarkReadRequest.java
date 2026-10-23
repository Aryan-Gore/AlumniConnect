package com.alumniconnect.backend.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Body of POST /chat/conversations/{id}/read and of the /app/chat.read frame. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MarkReadRequest {
    private Long conversationId;
    /** Optional: read up to this message; default is the latest one. */
    private Long messageId;
}
