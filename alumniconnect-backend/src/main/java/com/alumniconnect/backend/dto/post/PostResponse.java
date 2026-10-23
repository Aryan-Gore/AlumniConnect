package com.alumniconnect.backend.dto.post;

import com.alumniconnect.backend.dto.common.UserSummaryResponse;
import com.alumniconnect.backend.entity.PostVisibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class PostResponse {
    private Long id;
    private UserSummaryResponse author;
    private String content;
    private String imageUrl;
    private PostVisibility visibility;
    private Instant createdAt;
    private long likeCount;
    private long commentCount;
    private boolean likedByViewer;
    private boolean ownedByViewer;
}
