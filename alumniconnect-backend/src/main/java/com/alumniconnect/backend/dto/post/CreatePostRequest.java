package com.alumniconnect.backend.dto.post;

import com.alumniconnect.backend.entity.PostVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreatePostRequest {

    @NotBlank(message = "Post content cannot be blank.")
    @Size(min = 1, max = 5000, message = "Post content must be between 1 and 5000 characters.")
    private String content;

    private String imageUrl;

    @NotNull(message = "Visibility is required.")
    private PostVisibility visibility;
}
