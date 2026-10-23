package com.alumniconnect.backend.repository;

import com.alumniconnect.backend.entity.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, Long> {

    Optional<ConversationParticipant> findByConversationIdAndUserId(Long conversationId, Long userId);

    @Query("select p from ConversationParticipant p join fetch p.conversation where p.user.id = :userId")
    List<ConversationParticipant> findAllForUser(@Param("userId") Long userId);

    @Query("select p.user.id from ConversationParticipant p where p.conversation.id = :conversationId")
    List<Long> findUserIdsByConversation(@Param("conversationId") Long conversationId);

    /** [conversationId, otherUserId] for the DIRECT threads of the given conversations. */
    @Query("select p.conversation.id, p.user from ConversationParticipant p join p.user "
            + "where p.conversation.id in :ids and p.user.id <> :me")
    List<Object[]> findPeers(@Param("ids") Collection<Long> ids, @Param("me") Long me);

    /** [conversationId, participantCount]. */
    @Query("select p.conversation.id, count(p) from ConversationParticipant p "
            + "where p.conversation.id in :ids group by p.conversation.id")
    List<Object[]> countByConversation(@Param("ids") Collection<Long> ids);

    @Modifying
    @Query("delete from ConversationParticipant p where p.conversation.id = :conversationId and p.user.id = :userId")
    void removeMember(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}
