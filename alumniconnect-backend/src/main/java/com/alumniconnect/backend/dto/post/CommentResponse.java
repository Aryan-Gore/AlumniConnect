package com.alumniconnect.backend.dto.post;

import com.alumniconnect.backend.dto.common.UserSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class CommentResponse {
    private Long id;
    private Long postId;
    private UserSummaryResponse author;
    private String content;
    private Instant createdAt;
    private boolean ownedByViewer;
}
