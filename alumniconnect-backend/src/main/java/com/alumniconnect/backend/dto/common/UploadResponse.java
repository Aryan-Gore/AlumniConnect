package com.alumniconnect.backend.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UploadResponse {
    /** Path relative to the API base, e.g. /files/posts/3f2a….jpg — prefix it with the API base URL to display it. */
    private String url;
}
