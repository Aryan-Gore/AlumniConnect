package com.alumniconnect.backend.repository;

import com.alumniconnect.backend.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    Optional<PostLike> findByPostIdAndUserId(Long postId, Long userId);

    long countByPostId(Long postId);

    /** True if the given user liked this post. */
    boolean existsByPostIdAndUserId(Long postId, Long userId);

    @Modifying
    @Query("delete from PostLike pl where pl.post.id = :postId and pl.user.id = :userId")
    void deleteByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);

    /** Batch: for each post id, how many likes. */
    @Query("select pl.post.id, count(pl) from PostLike pl where pl.post.id in :postIds group by pl.post.id")
    List<Object[]> countByPostIds(@Param("postIds") Collection<Long> postIds);

    /** Batch: which of these posts did the viewer like? Returns the post ids they liked. */
    @Query("select pl.post.id from PostLike pl where pl.post.id in :postIds and pl.user.id = :userId")
    List<Long> likedPostIds(@Param("postIds") Collection<Long> postIds, @Param("userId") Long userId);
}
