package com.alumniconnect.backend.dto.mentorship;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MentorshipRequestCreate {

    @NotNull(message = "alumniId is required")
    private Long alumniId;

    @NotBlank(message = "Please include a short message")
    private String message;
}
