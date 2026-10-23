package com.alumniconnect.backend.controller;

import com.alumniconnect.backend.dto.post.CommentResponse;
import com.alumniconnect.backend.dto.post.CreateCommentRequest;
import com.alumniconnect.backend.dto.post.CreatePostRequest;
import com.alumniconnect.backend.dto.post.LikeResponse;
import com.alumniconnect.backend.dto.post.PostResponse;
import com.alumniconnect.backend.entity.User;
import com.alumniconnect.backend.security.CurrentUser;
import com.alumniconnect.backend.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Post CRUD, likes, and comments.
 *
 * POST   /posts                             — create post
 * GET    /posts/{id}                        — get post
 * DELETE /posts/{id}                        — delete post (author only)
 * POST   /posts/{id}/like                   — like post
 * DELETE /posts/{id}/like                   — unlike post
 * GET    /posts/{id}/comments               — list comments
 * POST   /posts/{id}/comments               — add comment
 * DELETE /posts/{postId}/comments/{commentId} — delete comment
 */
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // -----------------------------------------------------------------------
    // Posts
    // -----------------------------------------------------------------------

    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @CurrentUser User author,
            @Valid @RequestBody CreatePostRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.createPost(author, req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(
            @CurrentUser User caller,
            @PathVariable Long id) {
        return ResponseEntity.ok(postService.getPost(caller, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @CurrentUser User caller,
            @PathVariable Long id) {
        postService.deletePost(caller, id);
        return ResponseEntity.noContent().build();
    }

    // -----------------------------------------------------------------------
    // Likes
    // -----------------------------------------------------------------------

    @PostMapping("/{id}/like")
    public ResponseEntity<LikeResponse> likePost(
            @CurrentUser User caller,
            @PathVariable Long id) {
        return ResponseEntity.ok(postService.likePost(caller, id));
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<LikeResponse> unlikePost(
            @CurrentUser User caller,
            @PathVariable Long id) {
        return ResponseEntity.ok(postService.unlikePost(caller, id));
    }

    // -----------------------------------------------------------------------
    // Comments
    // -----------------------------------------------------------------------

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(
            @CurrentUser User caller,
            @PathVariable Long id) {
        return ResponseEntity.ok(postService.getComments(caller, id, false));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @CurrentUser User caller,
            @PathVariable Long id,
            @Valid @RequestBody CreateCommentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.addComment(caller, id, req));
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @CurrentUser User caller,
            @PathVariable Long postId,
            @PathVariable Long commentId) {
        postService.deleteComment(caller, postId, commentId);
        return ResponseEntity.noContent().build();
    }
}
