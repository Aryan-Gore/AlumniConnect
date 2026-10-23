package com.alumniconnect.backend.dto.chat;

import com.alumniconnect.backend.dto.common.DepartmentRef;
import com.alumniconnect.backend.dto.common.UserSummaryResponse;
import com.alumniconnect.backend.entity.ConversationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class ConversationResponse {
    private Long id;
    private ConversationType type;
    private String title;
    private String subtitle;
    private String avatarUrl;
    /** DIRECT only. */
    private UserSummaryResponse peer;
    /** DEPARTMENT only. */
    private DepartmentRef department;
    /** DEPARTMENT only: number of participants. */
    private Long memberCount;
    private MessageResponse lastMessage;
    private long unreadCount;
    private Instant updatedAt;
}
