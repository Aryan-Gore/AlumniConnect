package com.alumniconnect.backend.repository;

import com.alumniconnect.backend.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("select m from Message m join fetch m.sender where m.id = :id")
    java.util.Optional<Message> findWithSender(@Param("id") Long id);

    @Query("select max(m.id) from Message m where m.conversation.id = :conversationId")
    Long findLatestId(@Param("conversationId") Long conversationId);

    /** Newest first; pass Pageable.ofSize(limit + 1) to learn whether older messages exist. */
    @Query("select m from Message m join fetch m.sender where m.conversation.id = :conversationId "
            + "and (:before is null or m.id < :before) order by m.id desc")
    List<Message> findPage(@Param("conversationId") Long conversationId, @Param("before") Long before, Pageable pageable);

    /** The newest message of each given conversation. */
    @Query("select m from Message m join fetch m.sender where m.id in "
            + "(select max(m2.id) from Message m2 where m2.conversation.id in :ids group by m2.conversation.id)")
    List<Message> findLastMessages(@Param("ids") Collection<Long> ids);

    /** [conversationId, unreadCount] for every conversation the user takes part in that has unread messages. */
    @Query("select m.conversation.id, count(m) from Message m, ConversationParticipant p "
            + "where p.conversation = m.conversation and p.user.id = :userId and m.sender.id <> :userId "
            + "and m.id > coalesce(p.lastReadMessageId, 0) group by m.conversation.id")
    List<Object[]> unreadByConversation(@Param("userId") Long userId);

    @Query("select count(m) from Message m where m.conversation.id = :conversationId "
            + "and m.sender.id <> :userId and m.id > :after")
    long countUnread(@Param("conversationId") Long conversationId, @Param("userId") Long userId, @Param("after") Long after);
}
