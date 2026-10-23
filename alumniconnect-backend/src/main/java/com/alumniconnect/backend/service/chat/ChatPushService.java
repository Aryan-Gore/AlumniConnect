package com.alumniconnect.backend.service.chat;

import com.alumniconnect.backend.dto.chat.ChatEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

/**
 * Pushes ChatEvent frames to connected clients via STOMP /user/queue/messages.
 * <p>
 * All pushes are deferred until AFTER the enclosing transaction commits so that
 * the client can immediately call REST endpoints and find the persisted data.
 * If there is no active transaction the push fires synchronously (e.g. tests).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatPushService {

    private final SimpMessagingTemplate template;

    /**
     * Push a chat event to every listed recipient user id.
     * Called from inside a @Transactional service method; actual send is deferred
     * until afterCommit.
     */
    public void pushToUsers(List<Long> recipientUserIds, ChatEvent event) {
        if (recipientUserIds == null || recipientUserIds.isEmpty()) return;

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    doSend(recipientUserIds, event);
                }
            });
        } else {
            // No transaction active – send immediately (useful in tests / initialiser)
            doSend(recipientUserIds, event);
        }
    }

    private void doSend(List<Long> userIds, ChatEvent event) {
        for (Long userId : userIds) {
            try {
                template.convertAndSendToUser(
                        String.valueOf(userId),
                        "/queue/messages",
                        event);
            } catch (Exception ex) {
                // Log but do not rethrow — a disconnected user must not break delivery to others
                log.warn("Failed to push chat event to user {}: {}", userId, ex.getMessage());
            }
        }
    }
}
