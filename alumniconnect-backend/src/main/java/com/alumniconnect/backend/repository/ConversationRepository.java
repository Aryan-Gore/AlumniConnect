package com.alumniconnect.backend.repository;

import com.alumniconnect.backend.entity.Conversation;
import com.alumniconnect.backend.entity.ConversationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByDirectKey(String directKey);

    Optional<Conversation> findByDepartmentId(Long departmentId);

    List<Conversation> findByType(ConversationType type);

    /** Department rooms with their department and institute loaded (for titles and subtitles). */
    @Query("select c from Conversation c join fetch c.department d join fetch d.institute "
            + "where c.id in :ids")
    List<Conversation> findRoomsWithDepartment(@Param("ids") java.util.Collection<Long> ids);
}
