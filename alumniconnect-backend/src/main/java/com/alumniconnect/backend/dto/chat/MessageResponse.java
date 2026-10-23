package com.alumniconnect.backend.dto.chat;

import com.alumniconnect.backend.dto.common.UserSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class MessageResponse {
    private Long id;
    private Long conversationId;
    private UserSummaryResponse sender;
    private String content;
    private Instant sentAt;
    /** Echoed only to the sender's own copy so an optimistic bubble can be replaced. */
    private String clientId;

    /** The same message for someone who is not the sender: no client id. */
    public MessageResponse withoutClientId() {
        return new MessageResponse(id, conversationId, sender, content, sentAt, null);
    }

    public MessageResponse withClientId(String clientId) {
        return new MessageResponse(id, conversationId, sender, content, sentAt, clientId);
    }
}
