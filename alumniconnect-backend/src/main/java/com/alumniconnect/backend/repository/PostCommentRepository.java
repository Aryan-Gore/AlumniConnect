package com.alumniconnect.backend.repository;

import com.alumniconnect.backend.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    @Query("select c from PostComment c join fetch c.author where c.post.id = :postId order by c.id asc")
    List<PostComment> findByPostIdOrderByIdAsc(@Param("postId") Long postId);

    @Query("select c from PostComment c join fetch c.author where c.id = :id")
    Optional<PostComment> findWithAuthor(@Param("id") Long id);

    /** Batch: comment counts per post. */
    @Query("select c.post.id, count(c) from PostComment c where c.post.id in :postIds group by c.post.id")
    List<Object[]> countByPostIds(@Param("postIds") Collection<Long> postIds);

    long countByPostId(Long postId);
}
