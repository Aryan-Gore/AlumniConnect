package com.alumniconnect.backend.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReadReceiptResponse {
    private Long conversationId;
    private long unreadCount;
    private long totalUnread;
}
