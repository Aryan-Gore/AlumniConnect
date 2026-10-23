package com.alumniconnect.backend.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Pushed on /user/queue/errors when a /app/chat.send frame is rejected. */
@Getter
@AllArgsConstructor
public class ChatErrorResponse {
    private int status;
    private String message;
    private String clientId;
}
