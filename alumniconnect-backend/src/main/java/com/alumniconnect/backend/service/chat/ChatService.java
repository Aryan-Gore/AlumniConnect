package com.alumniconnect.backend.service.chat;

import com.alumniconnect.backend.dto.chat.ChatEvent;
import com.alumniconnect.backend.dto.chat.ConversationResponse;
import com.alumniconnect.backend.dto.chat.MessageResponse;
import com.alumniconnect.backend.dto.chat.ReadReceiptResponse;
import com.alumniconnect.backend.dto.chat.SendMessageRequest;
import com.alumniconnect.backend.dto.chat.StartDirectRequest;
import com.alumniconnect.backend.dto.chat.UnreadCountResponse;
import com.alumniconnect.backend.dto.common.CursorPage;
import com.alumniconnect.backend.dto.common.UserSummaryResponse;
import com.alumniconnect.backend.entity.Conversation;
import com.alumniconnect.backend.entity.ConversationParticipant;
import com.alumniconnect.backend.entity.ConversationType;
import com.alumniconnect.backend.entity.Message;
import com.alumniconnect.backend.entity.Role;
import com.alumniconnect.backend.entity.User;
import com.alumniconnect.backend.exception.BadRequestException;
import com.alumniconnect.backend.exception.ForbiddenException;
import com.alumniconnect.backend.exception.ResourceNotFoundException;
import com.alumniconnect.backend.repository.ConversationParticipantRepository;
import com.alumniconnect.backend.repository.ConversationRepository;
import com.alumniconnect.backend.repository.MessageRepository;
import com.alumniconnect.backend.repository.UserRepository;
import com.alumniconnect.backend.repository.ChatUserSearchRepository;
import com.alumniconnect.backend.service.AlumniSpecifications;
import com.alumniconnect.backend.service.UserSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Core chat logic: send messages, load conversations, mark read, user search.
 * Any authenticated user may chat; permission rules are enforced per method.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private static final int MAX_PAGE_SIZE = 50;
    private static final int DEFAULT_PAGE_SIZE = 30;

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatUserSearchRepository chatUserSearchRepository;
    private final ChatMapper chatMapper;
    private final ChatPushService pushService;
    private final DirectConversationCreator directCreator;
    private final DepartmentRoomService departmentRoomService;
    private final UserSummaryService userSummaryService;

    // -----------------------------------------------------------------------
    // Conversations list
    // -----------------------------------------------------------------------

    /**
     * Return all conversations the caller participates in, sorted by last activity
     * descending. Self-heals department room membership as a side-effect.
     * Caller: any authenticated user.
     */
    @Transactional
    public List<ConversationResponse> getConversations(User caller) {
        // Heal membership first (idempotent)
        departmentRoomService.syncMembership(caller);

        List<ConversationParticipant> participations = participantRepository.findAllForUser(caller.getId());
        if (participations.isEmpty()) return List.of();

        List<Long> convIds = participations.stream()
                .map(p -> p.getConversation().getId())
                .toList();

        // Batch-load last messages, unread counts, peer users, member counts
        List<Message> lastMessages = messageRepository.findLastMessages(convIds);
        Map<Long, MessageResponse> lastMsgByConv = buildLastMessageMap(lastMessages);

        List<Object[]> unreadRows = messageRepository.unreadByConversation(caller.getId());
        Map<Long, Long> unreadByConv = new HashMap<>();
        for (Object[] row : unreadRows) {
            unreadByConv.put((Long) row[0], (Long) row[1]);
        }

        // Separate DIRECT and DEPARTMENT conversation ids
        List<Long> directIds = participations.stream()
                .filter(p -> p.getConversation().getType() == ConversationType.DIRECT)
                .map(p -> p.getConversation().getId())
                .toList();
        List<Long> deptIds = participations.stream()
                .filter(p -> p.getConversation().getType() == ConversationType.DEPARTMENT)
                .map(p -> p.getConversation().getId())
                .toList();

        // Peers for DIRECT conversations
        Map<Long, User> peerByConv = new HashMap<>();
        if (!directIds.isEmpty()) {
            List<Object[]> peerRows = participantRepository.findPeers(directIds, caller.getId());
            for (Object[] row : peerRows) {
                Long convId = (Long) row[0];
                User peer = (User) row[1];
                peerByConv.put(convId, peer);
            }
        }

        // Member counts for DEPARTMENT conversations
        Map<Long, Long> memberCountByConv = new HashMap<>();
        if (!deptIds.isEmpty()) {
            List<Object[]> countRows = participantRepository.countByConversation(deptIds);
            for (Object[] row : countRows) {
                memberCountByConv.put((Long) row[0], (Long) row[1]);
            }
        }

        // Load department rooms with their department+institute
        Map<Long, Conversation> deptConvById = new HashMap<>();
        if (!deptIds.isEmpty()) {
            conversationRepository.findRoomsWithDepartment(deptIds)
                    .forEach(c -> deptConvById.put(c.getId(), c));
        }

        // Build responses
        List<ConversationResponse> responses = new ArrayList<>();

        for (ConversationParticipant p : participations) {
            Conversation conv = p.getConversation();
            long convId = conv.getId();
            MessageResponse lastMsg = lastMsgByConv.get(convId);
            long unread = unreadByConv.getOrDefault(convId, 0L);

            if (conv.getType() == ConversationType.DIRECT) {
                User peer = peerByConv.get(convId);
                if (peer == null) continue; // conversation has no peer yet (no messages sent)
                // DIRECT convs appear in inbox only after ≥1 message
                if (lastMsg == null) continue;
                responses.add(chatMapper.toDirectResponse(conv, peer, lastMsg, unread));
            } else {
                Conversation deptConv = deptConvById.getOrDefault(convId, conv);
                long memberCount = memberCountByConv.getOrDefault(convId, 0L);
                responses.add(chatMapper.toDepartmentResponse(deptConv, memberCount, lastMsg, unread));
            }
        }

        // Sort by lastMessage.sentAt DESC, then by conversation id DESC
        responses.sort((a, b) -> {
            if (a.getUpdatedAt() == null && b.getUpdatedAt() == null) return Long.compare(b.getId(), a.getId());
            if (a.getUpdatedAt() == null) return 1;
            if (b.getUpdatedAt() == null) return -1;
            int cmp = b.getUpdatedAt().compareTo(a.getUpdatedAt());
            return cmp != 0 ? cmp : Long.compare(b.getId(), a.getId());
        });

        return responses;
    }

    // -----------------------------------------------------------------------
    // Start / find a direct conversation
    // -----------------------------------------------------------------------

    /**
     * Find or create a DIRECT conversation with target user.
     * Caller: any authenticated user; cannot start with themselves.
     */
    @Transactional
    public ConversationResponse startDirect(User caller, StartDirectRequest req) {
        if (req.getUserId().equals(caller.getId())) {
            throw new BadRequestException("You cannot start a conversation with yourself.");
        }
        User target = userRepository.findById(req.getUserId())
                .filter(User::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        // Visibility check: alumni only visible to callers whose role permits it
        if (target.getRole() == Role.ALUMNI) {
            List<com.alumniconnect.backend.entity.DirectoryVisibility> visible =
                    AlumniSpecifications.visibleStatesFor(caller.getRole());
            // Use the search repo: if the target appears in a search for their own name it is visible
            String exactName = target.getFullName();
            boolean found = chatUserSearchRepository
                    .search(caller.getId(), exactName, visible, PageRequest.of(0, 1))
                    .getContent().stream()
                    .anyMatch(u -> u.getId().equals(target.getId()));
            if (!found) {
                throw new ResourceNotFoundException("User not found.");
            }
        }

        Conversation conv = directCreator.findOrCreate(caller.getId(), target.getId());
        MessageResponse lastMsg = getLastMessageResponse(conv.getId());
        long unread = countUnread(conv.getId(), caller.getId());
        return chatMapper.toDirectResponse(conv, target, lastMsg, unread);
    }

    // -----------------------------------------------------------------------
    // Messages
    // -----------------------------------------------------------------------

    /**
     * Load a page of messages in a conversation, newest-first (cursor = before message id).
     * Caller: must be a participant.
     */
    public CursorPage<MessageResponse> getMessages(User caller, Long conversationId, Long before, int size) {
        requireParticipant(caller.getId(), conversationId);
        int pageSize = Math.min(size <= 0 ? DEFAULT_PAGE_SIZE : size, MAX_PAGE_SIZE);
        PageRequest pageable = PageRequest.of(0, pageSize + 1); // fetch one extra to detect more
        List<Message> messages = messageRepository.findPage(conversationId, before, pageable);
        boolean hasMore = messages.size() > pageSize;
        List<Message> page = hasMore ? messages.subList(0, pageSize) : messages;
        List<MessageResponse> dtos = chatMapper.toMessageResponses(page);
        Long nextBefore = hasMore ? page.get(page.size() - 1).getId() : null;
        return CursorPage.of(dtos, nextBefore);
    }

    /**
     * Persist and broadcast a new message.
     * Caller: must be a participant; content must be non-blank.
     */
    @Transactional
    public MessageResponse sendMessage(User caller, SendMessageRequest req) {
        if (req.getContent() == null || req.getContent().isBlank()) {
            throw new BadRequestException("Message content cannot be blank.");
        }
        String trimmed = req.getContent().trim();
        if (trimmed.length() > 2000) {
            throw new BadRequestException("Message must not exceed 2000 characters.");
        }

        Conversation conv = conversationRepository.findById(req.getConversationId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found."));

        requireParticipant(caller.getId(), conv.getId());

        Message message = new Message();
        message.setConversation(conv);
        message.setSender(caller);
        message.setContent(trimmed);
        messageRepository.save(message);

        MessageResponse response = chatMapper.toMessageResponse(message);
        MessageResponse withClientId = response.withClientId(req.getClientId());

        // Push to all participants after commit
        List<Long> participantIds = participantRepository.findUserIdsByConversation(conv.getId());
        for (Long uid : participantIds) {
            MessageResponse payload = uid.equals(caller.getId()) ? withClientId : response.withoutClientId();
            pushService.pushToUsers(List.of(uid), ChatEvent.message(payload));
        }

        return withClientId;
    }

    /**
     * Mark messages read up to (optionally) a specific message id in a conversation.
     * Caller: must be a participant.
     */
    @Transactional
    public ReadReceiptResponse markRead(User caller, Long conversationId, Long messageId) {
        ConversationParticipant p = requireParticipant(caller.getId(), conversationId);
        Long latestId = messageId != null ? messageId : messageRepository.findLatestId(conversationId);
        if (latestId != null) {
            p.setLastReadMessageId(latestId);
            participantRepository.save(p);
        }
        long unreadInConv = latestId != null
                ? messageRepository.countUnread(conversationId, caller.getId(), latestId)
                : 0L;
        long totalUnread = calculateTotalUnread(caller.getId());

        // Broadcast READ event to other participants so they can update receipts
        List<Long> others = participantRepository.findUserIdsByConversation(conversationId)
                .stream().filter(uid -> !uid.equals(caller.getId())).toList();
        pushService.pushToUsers(others, ChatEvent.read(conversationId));

        return new ReadReceiptResponse(conversationId, unreadInConv, totalUnread);
    }

    // -----------------------------------------------------------------------
    // Unread count
    // -----------------------------------------------------------------------

    /**
     * Total unread message count across all conversations.
     * Caller: any authenticated user.
     */
    public UnreadCountResponse getUnreadCount(User caller) {
        return new UnreadCountResponse(calculateTotalUnread(caller.getId()));
    }

    // -----------------------------------------------------------------------
    // User search for new DMs
    // -----------------------------------------------------------------------

    /**
     * Search users to start a chat with. Returns up to 20 results.
     * Alumni visibility rules are enforced via ChatUserSearchRepository.
     * Caller: any authenticated user.
     */
    public List<UserSummaryResponse> searchUsers(User caller, String q) {
        if (q == null || q.isBlank()) return List.of();
        String trimmed = q.trim();
        List<com.alumniconnect.backend.entity.DirectoryVisibility> visible =
                AlumniSpecifications.visibleStatesFor(caller.getRole());
        PageRequest pageable = PageRequest.of(0, 20);
        org.springframework.data.domain.Page<User> page =
                chatUserSearchRepository.search(caller.getId(), trimmed, visible, pageable);
        List<User> users = page.getContent();
        Map<Long, UserSummaryResponse> summaries = userSummaryService.summarizeAll(users);
        return users.stream().map(u -> summaries.get(u.getId())).toList();
    }

    // -----------------------------------------------------------------------
    // Admin: delete a message
    // -----------------------------------------------------------------------

    /**
     * Admin removes any message from a department room. Direct messages are not accessible to admins.
     * Caller: ADMIN only.
     */
    @Transactional
    public void adminDeleteMessage(User admin, Long messageId) {
        if (admin.getRole() != Role.ADMIN) throw new ForbiddenException("Admin access required.");
        Message msg = messageRepository.findWithSender(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found."));
        Conversation conv = msg.getConversation();
        if (conv.getType() != ConversationType.DEPARTMENT) {
            throw new ForbiddenException("Admins may only remove messages from department rooms.");
        }
        messageRepository.delete(msg);

        // Push deletion event to all participants
        List<Long> participantIds = participantRepository.findUserIdsByConversation(conv.getId());
        pushService.pushToUsers(participantIds, ChatEvent.deleted(conv.getId(), messageId));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private ConversationParticipant requireParticipant(Long userId, Long conversationId) {
        return participantRepository.findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found."));
    }

    private long calculateTotalUnread(Long userId) {
        List<Object[]> rows = messageRepository.unreadByConversation(userId);
        return rows.stream().mapToLong(r -> (Long) r[1]).sum();
    }

    private MessageResponse getLastMessageResponse(Long conversationId) {
        List<Message> msgs = messageRepository.findLastMessages(List.of(conversationId));
        if (msgs.isEmpty()) return null;
        return chatMapper.toMessageResponse(msgs.get(0));
    }

    private long countUnread(Long conversationId, Long userId) {
        ConversationParticipant p = participantRepository
                .findByConversationIdAndUserId(conversationId, userId)
                .orElse(null);
        if (p == null) return 0;
        long after = p.getLastReadMessageId() != null ? p.getLastReadMessageId() : 0L;
        return messageRepository.countUnread(conversationId, userId, after);
    }

    /**
     * Given a batch of last-messages (one per conversation, already sorted
     * newest-first by the repository query), returns a map of conversationId →
     * mapped MessageResponse for O(1) lookup while building the response list.
     *
     * The repository query returns at most one row per conversation, so the
     * stream-to-map collector never encounters a duplicate key.
     */
    private Map<Long, MessageResponse> buildLastMessageMap(List<Message> lastMessages) {
        return lastMessages.stream()
                .collect(Collectors.toMap(
                        m -> m.getConversation().getId(),
                        chatMapper::toMessageResponse,
                        (existing, replacement) -> existing   // keep first if somehow duplicated
                ));
    }

}
