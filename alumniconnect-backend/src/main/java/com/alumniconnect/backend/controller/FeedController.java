package com.alumniconnect.backend.controller;

import com.alumniconnect.backend.dto.common.CursorPage;
import com.alumniconnect.backend.dto.post.PostResponse;
import com.alumniconnect.backend.entity.User;
import com.alumniconnect.backend.security.CurrentUser;
import com.alumniconnect.backend.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * GET /feed — main social feed, paginated.
 * Optional filters: departmentId (group feed), authorId (profile Posts tab).
 */
@RestController
@RequestMapping("/feed")
@RequiredArgsConstructor
public class FeedController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<CursorPage<PostResponse>> getFeed(
            @CurrentUser User caller,
            @RequestParam(required = false) Long before,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long authorId) {

        return ResponseEntity.ok(
                postService.getFeed(caller, before, limit, departmentId, authorId));
    }
}
