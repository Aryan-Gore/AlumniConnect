package com.alumniconnect.backend.service.chat;

import com.alumniconnect.backend.dto.chat.ConversationResponse;
import com.alumniconnect.backend.dto.chat.MessageResponse;
import com.alumniconnect.backend.dto.common.DepartmentRef;
import com.alumniconnect.backend.entity.Conversation;
import com.alumniconnect.backend.entity.ConversationType;
import com.alumniconnect.backend.entity.Message;
import com.alumniconnect.backend.entity.User;
import com.alumniconnect.backend.service.UserSummaryService;
import com.alumniconnect.backend.dto.common.UserSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Maps chat entities to DTOs. All methods must be called inside an active transaction
 * because conversation.department may be lazily loaded.
 */
@Component
@RequiredArgsConstructor
public class ChatMapper {

    private final UserSummaryService userSummaryService;

    /** Map a single Message → MessageResponse. The message's sender must already be fetched. */
    public MessageResponse toMessageResponse(Message message) {
        UserSummaryResponse senderSummary = userSummaryService.summarize(message.getSender());
        return MessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .sender(senderSummary)
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .build();
    }

    /**
     * Map a list of messages whose senders are already fetched.
     * Batches user summary lookups for all distinct senders.
     */
    public List<MessageResponse> toMessageResponses(List<Message> messages) {
        if (messages.isEmpty()) return List.of();
        Collection<User> senders = messages.stream()
                .map(Message::getSender)
                .distinct()
                .toList();
        Map<Long, UserSummaryResponse> summaries = userSummaryService.summarizeAll(senders);
        return messages.stream()
                .map(m -> MessageResponse.builder()
                        .id(m.getId())
                        .conversationId(m.getConversation().getId())
                        .sender(summaries.get(m.getSender().getId()))
                        .content(m.getContent())
                        .sentAt(m.getSentAt())
                        .build())
                .toList();
    }

    /**
     * Build a ConversationResponse for a DIRECT conversation.
     * peer is the other participant (already a fetched User).
     */
    public ConversationResponse toDirectResponse(
            Conversation conv,
            User peer,
            MessageResponse lastMessage,
            long unreadCount) {
        UserSummaryResponse peerSummary = userSummaryService.summarize(peer);
        return ConversationResponse.builder()
                .id(conv.getId())
                .type(ConversationType.DIRECT)
                .title(peer.getFullName())
                .subtitle(peerSummary.getHeadline())
                .avatarUrl(peer.getProfilePhotoUrl())
                .peer(peerSummary)
                .memberCount(2L)
                .lastMessage(lastMessage)
                .unreadCount(unreadCount)
                .updatedAt(lastMessage != null ? lastMessage.getSentAt() : conv.getCreatedAt())
                .build();
    }

    /**
     * Build a ConversationResponse for a DEPARTMENT conversation.
     * conv.department (and department.institute) must be initialised.
     */
    public ConversationResponse toDepartmentResponse(
            Conversation conv,
            Long memberCount,
            MessageResponse lastMessage,
            long unreadCount) {
        DepartmentRef deptRef = DepartmentRef.from(conv.getDepartment());
        String subtitle = conv.getDepartment().getInstitute().getName();
        return ConversationResponse.builder()
                .id(conv.getId())
                .type(ConversationType.DEPARTMENT)
                .title(conv.getDepartment().getName() + " Group")
                .subtitle(subtitle)
                .department(deptRef)
                .memberCount(memberCount)
                .lastMessage(lastMessage)
                .unreadCount(unreadCount)
                .updatedAt(lastMessage != null ? lastMessage.getSentAt() : conv.getCreatedAt())
                .build();
    }
}
