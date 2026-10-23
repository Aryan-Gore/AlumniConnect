package com.alumniconnect.backend.repository;

import com.alumniconnect.backend.entity.DirectoryVisibility;
import com.alumniconnect.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

/** "Who can I start a chat with?" — a people search that honours the alumni directory's privacy levels. */
public interface ChatUserSearchRepository extends JpaRepository<User, Long> {

    /**
     * Active accounts whose name contains the (already LIKE-escaped, backslash-escaped) text. Students and admins are
     * always found; an alumnus only when their record's directory visibility is one the viewer may see.
     */
    @Query("select u from User u where u.active = true and u.id <> :me "
            + "and lower(u.fullName) like lower(concat('%', :q, '%')) escape '\\' "
            + "and (u.role <> com.alumniconnect.backend.entity.Role.ALUMNI "
            + "     or exists (select 1 from AlumniRecord a where a.user = u and a.directoryVisibility in :visible)) "
            + "order by u.fullName asc, u.id asc")
    Page<User> search(@Param("me") Long me, @Param("q") String q,
                      @Param("visible") Collection<DirectoryVisibility> visible, Pageable pageable);
}
