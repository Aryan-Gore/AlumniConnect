package com.alumniconnect.backend.repository;

import com.alumniconnect.backend.entity.Post;
import com.alumniconnect.backend.entity.PostVisibility;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("select p from Post p join fetch p.author where p.id = :id")
    Optional<Post> findWithAuthor(@Param("id") Long id);

    /**
     * Feed query: visibility-filtered, cursor-paginated (newest first).
     * Only posts by active authors, and only those whose visibility the caller may see.
     * Caller's own posts are always included via the authorId OR clause.
     */
    @Query("""
            select p from Post p join fetch p.author a
            where a.active = true
              and (:before is null or p.id < :before)
              and (p.visibility in :visible or p.author.id = :callerId)
            order by p.id desc
            """)
    List<Post> findFeedPage(
            @Param("callerId") Long callerId,
            @Param("visible") Collection<PostVisibility> visible,
            @Param("before") Long before,
            Pageable pageable);

    /**
     * Feed filtered by a specific department (group feed): posts by members of that department.
     */
    @Query("""
            select p from Post p join fetch p.author a
            where a.active = true
              and (:before is null or p.id < :before)
              and (p.visibility in :visible or p.author.id = :callerId)
              and (
                exists (select 1 from StudentProfile sp where sp.user = a and sp.department.id = :deptId)
                or exists (select 1 from AlumniRecord ar where ar.user = a and ar.department.id = :deptId and ar.status = com.alumniconnect.backend.entity.AlumniStatus.ACTIVE)
              )
            order by p.id desc
            """)
    List<Post> findFeedByDepartment(
            @Param("callerId") Long callerId,
            @Param("visible") Collection<PostVisibility> visible,
            @Param("before") Long before,
            @Param("deptId") Long deptId,
            Pageable pageable);

    /**
     * Feed filtered by a specific author (profile Posts tab).
     */
    @Query("""
            select p from Post p join fetch p.author a
            where a.active = true
              and p.author.id = :authorId
              and (:before is null or p.id < :before)
              and (p.visibility in :visible or p.author.id = :callerId)
            order by p.id desc
            """)
    List<Post> findFeedByAuthor(
            @Param("callerId") Long callerId,
            @Param("visible") Collection<PostVisibility> visible,
            @Param("before") Long before,
            @Param("authorId") Long authorId,
            Pageable pageable);

    /** Admin feed: all posts, any visibility, any author state, newest first. */
    @Query("""
            select p from Post p join fetch p.author a
            where (:before is null or p.id < :before)
              and (:query is null or lower(p.content) like lower(concat('%', :query, '%'))
                   or lower(a.fullName) like lower(concat('%', :query, '%')))
            order by p.id desc
            """)
    List<Post> findAdminFeed(
            @Param("before") Long before,
            @Param("query") String query,
            Pageable pageable);

    @Query("select count(p) from Post p where p.author.id = :authorId")
    long countByAuthorId(@Param("authorId") Long authorId);
}
