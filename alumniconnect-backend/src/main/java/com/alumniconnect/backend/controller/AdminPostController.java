package com.alumniconnect.backend.controller;

import com.alumniconnect.backend.dto.common.CursorPage;
import com.alumniconnect.backend.dto.post.CommentResponse;
import com.alumniconnect.backend.dto.post.PostResponse;
import com.alumniconnect.backend.entity.User;
import com.alumniconnect.backend.security.CurrentUser;
import com.alumniconnect.backend.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin-only post moderation.
 *
 * GET    /admin/posts                             — all posts (any visibility, cursor-paginated)
 * DELETE /admin/posts/{postId}                    — remove any post
 * GET    /admin/posts/{postId}/comments           — all comments on a post
 * DELETE /admin/posts/{postId}/comments/{commentId} — remove any comment
 */
@RestController
@RequestMapping("/admin/posts")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminPostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<CursorPage<PostResponse>> listPosts(
            @CurrentUser User admin,
            @RequestParam(required = false) Long before,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String query) {
        return ResponseEntity.ok(postService.getAdminFeed(admin, before, limit, query));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @CurrentUser User admin,
            @PathVariable Long postId) {
        postService.adminDeletePost(admin, postId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> listComments(
            @CurrentUser User admin,
            @PathVariable Long postId) {
        return ResponseEntity.ok(postService.getComments(admin, postId, true));
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @CurrentUser User admin,
            @PathVariable Long postId,
            @PathVariable Long commentId) {
        postService.adminDeleteComment(admin, postId, commentId);
        return ResponseEntity.noContent().build();
    }
}
