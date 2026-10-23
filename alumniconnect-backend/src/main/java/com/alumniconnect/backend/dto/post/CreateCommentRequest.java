package com.alumniconnect.backend.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateCommentRequest {

    @NotBlank(message = "Comment content cannot be blank.")
    @Size(min = 1, max = 2000, message = "Comment must be between 1 and 2000 characters.")
    private String content;
}
