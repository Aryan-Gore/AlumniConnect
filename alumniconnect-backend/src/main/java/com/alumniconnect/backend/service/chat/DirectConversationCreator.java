package com.alumniconnect.backend.service.chat;

import com.alumniconnect.backend.entity.Conversation;
import com.alumniconnect.backend.entity.ConversationParticipant;
import com.alumniconnect.backend.entity.ConversationType;
import com.alumniconnect.backend.entity.User;
import com.alumniconnect.backend.repository.ConversationParticipantRepository;
import com.alumniconnect.backend.repository.ConversationRepository;
import com.alumniconnect.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Isolated REQUIRES_NEW bean for race-safe find-or-create of DIRECT conversations.
 * <p>
 * Using a separate propagation ensures the insert commits (or rolls back on a
 * duplicate-key violation from a concurrent request) before the caller proceeds,
 * so a second read always finds the already-committed row.
 */
@Service
@RequiredArgsConstructor
public class DirectConversationCreator {

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final UserRepository userRepository;

    /**
     * Return the existing DIRECT conversation between the two users, or create one.
     * directKey is canonical: "lowerUserId:higherUserId".
     * Caller: any role; both user ids must exist.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Conversation findOrCreate(Long userIdA, Long userIdB) {
        long lower = Math.min(userIdA, userIdB);
        long higher = Math.max(userIdA, userIdB);
        String directKey = lower + ":" + higher;

        // First attempt: find existing
        return conversationRepository.findByDirectKey(directKey)
                .orElseGet(() -> {
                    Conversation conv = new Conversation();
                    conv.setType(ConversationType.DIRECT);
                    conv.setDirectKey(directKey);
                    try {
                        conv = conversationRepository.save(conv);
                    } catch (Exception ex) {
                        // Another thread may have created it concurrently — re-read
                        return conversationRepository.findByDirectKey(directKey)
                                .orElseThrow(() -> new IllegalStateException(
                                        "Failed to find or create direct conversation for key " + directKey, ex));
                    }
                    // Add both participants
                    User userA = userRepository.getReferenceById(userIdA);
                    User userB = userRepository.getReferenceById(userIdB);
                    ConversationParticipant pa = new ConversationParticipant();
                    pa.setConversation(conv);
                    pa.setUser(userA);
                    ConversationParticipant pb = new ConversationParticipant();
                    pb.setConversation(conv);
                    pb.setUser(userB);
                    participantRepository.save(pa);
                    participantRepository.save(pb);
                    return conv;
                });
    }
}
